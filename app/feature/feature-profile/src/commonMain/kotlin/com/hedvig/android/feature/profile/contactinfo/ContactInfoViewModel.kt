package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.RetryLoadData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.SubmitData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Content
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Loading
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Error
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Fetching
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Idle
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInformation
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.android.feature.profile.data.valueForTextField
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay

internal sealed interface ContactInfoEvent {
  data object RetryLoadData : ContactInfoEvent

  data object SubmitData : ContactInfoEvent

  data object ShowedMessage : ContactInfoEvent

  data class ChangePhoneNumber(val phoneNumber: String) : ContactInfoEvent

  data class ChangeEmail(val email: String) : ContactInfoEvent
}

internal sealed interface ContactInfoUiState {
  val content: Content?
    get() = this as? Content

  data object Loading : ContactInfoUiState

  data object Error : ContactInfoUiState

  /**
   * [phoneNumberRevision] and [emailRevision] increase only when the presenter itself writes that text, so the UI
   * copies [phoneNumber] or [email] into its field on a revision change and otherwise leaves what the member typed.
   */
  data class Content(
    val phoneNumber: String,
    val email: String,
    val uploadedPhoneNumber: String?,
    val uploadedEmail: Email?,
    val submittingUpdatedInfo: Boolean,
    val showSuccessSnackBar: Boolean = false,
    val errorSnackBarText: ErrorSnackBarText? = null,
    val hasAttemptedSubmission: Boolean = false,
    val phoneNumberRevision: Int = 0,
    val emailRevision: Int = 0,
  ) : ContactInfoUiState {
    /**
     * Validated with the same rules the submission uses, so that an input can never look valid but be rejected on
     * submit. An empty field only counts as an error once submission has been attempted, since the member has not
     * had the chance to fill it in before that.
     */
    private val validatedPhoneNumber: Either<ErrorMessage, PhoneNumber?>
      get() = when {
        phoneNumber.isBlank() && !hasAttemptedSubmission -> null.right()
        else -> PhoneNumber.notNullFromString(phoneNumber)
      }
    private val validatedEmail: Either<ErrorMessage, Email?>
      get() = when {
        email.isBlank() && !hasAttemptedSubmission -> null.right()
        else -> Email.fromStringNotNull(email)
      }

    val phoneNumberHasError: Boolean
      get() = validatedPhoneNumber.isLeft()
    val emailHasError: Boolean
      get() = validatedEmail.isLeft()
  }
}

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ContactInfoViewModel(
  repository: ContactInfoRepository,
  clock: Clock,
) : MoleculeViewModel<ContactInfoEvent, ContactInfoUiState>(
    Loading,
    ContactInfoPresenter(repository, clock),
  )

internal class ContactInfoPresenter(
  private val repository: ContactInfoRepository,
  private val clock: Clock,
) : MoleculePresenter<ContactInfoEvent, ContactInfoUiState> {
  @Composable
  override fun MoleculePresenterScope<ContactInfoEvent>.present(lastState: ContactInfoUiState): ContactInfoUiState {
    var phoneNumber by remember { mutableStateOf(lastState.content?.phoneNumber ?: "") }
    var phoneNumberRevision by remember { mutableIntStateOf(lastState.content?.phoneNumberRevision ?: 0) }
    var email by remember { mutableStateOf(lastState.content?.email ?: "") }
    var emailRevision by remember { mutableIntStateOf(lastState.content?.emailRevision ?: 0) }
    var uploadedEmail: Email? by remember { mutableStateOf(lastState.content?.uploadedEmail) }
    var uploadedPhoneNumber: String? by remember { mutableStateOf(lastState.content?.uploadedPhoneNumber) }

    var refetchDataIteration by remember { mutableIntStateOf(0) }

    var submittingData: Pair<PhoneNumber, Email>? by remember { mutableStateOf(null) }
    var hasAttemptedSubmission by remember { mutableStateOf(lastState.content?.hasAttemptedSubmission == true) }
    var errorSnackBarText by remember { mutableStateOf<ErrorSnackBarText?>(null) }
    var showSuccessToast by remember { mutableStateOf<Boolean>(false) }

    var dataFetchingState by remember {
      mutableStateOf(
        when (lastState) {
          is Content -> Idle
          ContactInfoUiState.Error -> Error
          Loading -> Fetching
        },
      )
    }

    val updateStateWithFetchedContactInformation = { contactInformation: ContactInformation ->
      dataFetchingState = Idle
      uploadedEmail = contactInformation.email
      uploadedPhoneNumber = contactInformation.phoneNumber
      email = contactInformation.email.valueForTextField
      emailRevision++
      phoneNumber = contactInformation.phoneNumber ?: ""
      phoneNumberRevision++
    }

    LaunchedEffect(refetchDataIteration) {
      if (refetchDataIteration == 0 && lastState is Content) {
        return@LaunchedEffect
      }
      errorSnackBarText = null
      dataFetchingState = Fetching
      repository.contactInfo().fold(
        ifLeft = {
          dataFetchingState = Error
        },
        ifRight = { contactInformation ->
          Snapshot.withMutableSnapshot {
            updateStateWithFetchedContactInformation(contactInformation)
          }
        },
      )
    }

    if (submittingData != null) {
      LaunchedEffect(submittingData) {
        val (submittingPhoneNumber, submittingEmail) = submittingData!!
        val retrySubmissionStart = if (errorSnackBarText != null) {
          clock.now()
        } else {
          null
        }
        errorSnackBarText = null
        repository
          .updateInfo(
            phoneNumber = submittingPhoneNumber,
            email = submittingEmail,
          )
          .fold(
            ifLeft = { error ->
              if (retrySubmissionStart != null) {
                delayAtLeastOneSecondToShowThatTheNewRequestFailedAgain(retrySubmissionStart)
              }
              val text = error.message
              if (text == null) {
                Snapshot.withMutableSnapshot {
                  submittingData = null
                  errorSnackBarText = ErrorSnackBarText.General
                }
              } else {
                Snapshot.withMutableSnapshot {
                  submittingData = null
                  errorSnackBarText = ErrorSnackBarText.WithMessage(text)
                }
              }
            },
            ifRight = { contactInformation ->
              Snapshot.withMutableSnapshot {
                updateStateWithFetchedContactInformation(contactInformation)
                submittingData = null
                showSuccessToast = true
              }
            },
          )
      }
    }

    CollectEvents { event ->
      when (event) {
        RetryLoadData -> {
          refetchDataIteration++
        }

        SubmitData -> {
          // Marked before validating, so that invalid input the member has now submitted starts showing its error.
          hasAttemptedSubmission = true
          if (submittingData != null) return@CollectEvents
          val trimmedPhoneNumber = PhoneNumber
            .notNullFromString(phoneNumber)
            .getOrElse {
              return@CollectEvents
            }
          val emailValue = Email.fromStringNotNull(email).getOrElse {
            return@CollectEvents
          }
          submittingData = trimmedPhoneNumber to emailValue
        }

        is ContactInfoEvent.ChangePhoneNumber -> {
          phoneNumber = event.phoneNumber
        }

        is ContactInfoEvent.ChangeEmail -> {
          // An email address never starts or ends with whitespace, whichever keyboard or paste it came from.
          email = event.email.trim()
        }

        ContactInfoEvent.ShowedMessage -> {
          Snapshot.withMutableSnapshot {
            errorSnackBarText = null
            showSuccessToast = false
          }
        }
      }
    }

    return when (dataFetchingState) {
      Error -> ContactInfoUiState.Error

      Fetching -> Loading

      Idle -> Content(
        email = email,
        emailRevision = emailRevision,
        phoneNumber = phoneNumber,
        phoneNumberRevision = phoneNumberRevision,
        uploadedEmail = uploadedEmail,
        uploadedPhoneNumber = uploadedPhoneNumber,
        submittingUpdatedInfo = submittingData != null,
        errorSnackBarText = errorSnackBarText,
        showSuccessSnackBar = showSuccessToast,
        hasAttemptedSubmission = hasAttemptedSubmission,
      )
    }
  }

  private suspend fun delayAtLeastOneSecondToShowThatTheNewRequestFailedAgain(submissionStart: Instant) {
    val retrySubmissionEnd = clock.now()
    delay(1.seconds - (retrySubmissionEnd - submissionStart))
  }
}

private enum class DataFetchingState {
  Idle,
  Fetching,
  Error,
}

internal sealed interface ErrorSnackBarText {
  data object General : ErrorSnackBarText

  data class WithMessage(val message: String) : ErrorSnackBarText
}
