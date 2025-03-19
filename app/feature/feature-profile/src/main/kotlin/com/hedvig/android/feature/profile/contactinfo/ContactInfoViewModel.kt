package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import arrow.core.Either
import arrow.core.getOrElse
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.RetryLoadData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoEvent.SubmitData
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Content
import com.hedvig.android.feature.profile.contactinfo.ContactInfoUiState.Loading
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Error
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Fetching
import com.hedvig.android.feature.profile.contactinfo.DataFetchingState.Idle
import com.hedvig.android.feature.profile.data.ContactInfoRepository
import com.hedvig.android.feature.profile.data.ContactInfoRepository.UpdateFailure
import com.hedvig.android.feature.profile.data.ContactInformation
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.android.feature.profile.data.valueForTextField
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal sealed interface ContactInfoEvent {
  data object RetryLoadData : ContactInfoEvent

  data object SubmitData : ContactInfoEvent
}

internal sealed interface ContactInfoUiState {
  val content: Content?
    get() = this as? Content

  data object Loading : ContactInfoUiState

  data object Error : ContactInfoUiState

  data class Content(
    val phoneNumberState: TextFieldState,
    val emailState: TextFieldState,
    val uploadedPhoneNumber: PhoneNumber?,
    val uploadedEmail: Email?,
    val submittingUpdatedInfo: Boolean,
  ) : ContactInfoUiState {
    private val phoneNumber: Either<ErrorMessage, PhoneNumber?>
      get() = PhoneNumber.fromStringAfterTrimmingWhitespaces(phoneNumberState.text.toString())
    private val email: Either<ErrorMessage, Email?>
      get() = Email.fromString(emailState.text.toString())

    val phoneNumberHasError: Boolean
      get() = phoneNumber.isLeft()
    val emailHasError: Boolean
      get() = email.isLeft()

    private val emailIsDifferentFromUploadedEmail: Boolean
      get() = uploadedEmail.valueForTextField != emailState.text
    private val phoneNumberIsDifferentFromUploadedPhoneNumber: Boolean
      get() = uploadedPhoneNumber.valueForTextField != phoneNumberState.text

    private val emailIsDeletingKnownInfo: Boolean
      get() = when (uploadedEmail) {
        null -> false
        else -> emailState.text.isBlank()
      }
    private val phoneNumberIsDeletingKnownInfo: Boolean
      get() = when (uploadedPhoneNumber) {
        null -> false
        else -> phoneNumberState.text.isBlank()
      }

    val canSubmit: Boolean
      get() = (emailIsDifferentFromUploadedEmail || phoneNumberIsDifferentFromUploadedPhoneNumber) &&
        !emailIsDeletingKnownInfo &&
        !phoneNumberIsDeletingKnownInfo &&
        !emailHasError &&
        !phoneNumberHasError &&
        !submittingUpdatedInfo

    val phoneNumberInputTransformation = InputTransformation.byValue { _, proposed ->
      proposed.filterNot { it.isWhitespace() }.trim()
    }
    val emailInputTransformation = InputTransformation.byValue { _, proposed ->
      proposed.trim()
    }
  }
}

internal class ContactInfoViewModel(
  private val repository: Provider<ContactInfoRepository>,
) : MoleculeViewModel<ContactInfoEvent, ContactInfoUiState>(
    ContactInfoUiState.Loading,
    ContactInfoPresenter(repository),
  )

internal class ContactInfoPresenter(
  private val repository: Provider<ContactInfoRepository>,
) : MoleculePresenter<ContactInfoEvent, ContactInfoUiState> {
  @Composable
  override fun MoleculePresenterScope<ContactInfoEvent>.present(lastState: ContactInfoUiState): ContactInfoUiState {
    val phoneNumber = remember {
      val lastPhoneNumberState = lastState.content?.phoneNumberState
      TextFieldState(lastPhoneNumberState?.text?.toString() ?: "", lastPhoneNumberState?.selection ?: TextRange(0))
    }
    val email = remember {
      val lastEmailState = lastState.content?.emailState
      TextFieldState(lastEmailState?.text?.toString() ?: "", lastEmailState?.selection ?: TextRange(0))
    }
    var uploadedEmail: Email? by remember { mutableStateOf(lastState.content?.uploadedEmail) }
    var uploadedPhoneNumber: PhoneNumber? by remember { mutableStateOf(lastState.content?.uploadedPhoneNumber) }

    var refetchDataIteration by remember { mutableIntStateOf(0) }

    var submittingData: Pair<PhoneNumber?, Email?>? by remember { mutableStateOf(null) }
    var submissionError by remember { mutableStateOf<Boolean>(false) }

    var dataFetchingState by remember {
      mutableStateOf(
        when (lastState) {
          is Content -> DataFetchingState.Idle
          ContactInfoUiState.Error -> DataFetchingState.Error
          Loading -> DataFetchingState.Fetching
        },
      )
    }

    val updateStateWithFetchedContactInformation = { contactInformation: ContactInformation ->
      dataFetchingState = DataFetchingState.Idle
      uploadedEmail = contactInformation.email
      uploadedPhoneNumber = contactInformation.phoneNumber
      email.setTextAndPlaceCursorAtEnd(contactInformation.email.valueForTextField)
      phoneNumber.setTextAndPlaceCursorAtEnd(contactInformation.phoneNumber.valueForTextField)
    }

    LaunchedEffect(refetchDataIteration) {
      if (refetchDataIteration == 0 && lastState is ContactInfoUiState.Content) {
        return@LaunchedEffect
      }
      dataFetchingState = DataFetchingState.Fetching
      repository.provide().contactInfo().fold(
        ifLeft = {
          dataFetchingState = DataFetchingState.Error
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
        repository
          .provide()
          .updateInfo(
            phoneNumber = submittingPhoneNumber,
            email = submittingEmail,
            originalNumber = uploadedPhoneNumber!!,
            originalEmail = uploadedEmail!!,
          )
          .fold(
            ifLeft = { error ->
              when (error) {
                // no-op
                UpdateFailure.NoChanges -> {}
                is UpdateFailure.Error -> {
                  Snapshot.withMutableSnapshot {
                    submissionError = true
                    submittingData = null
                  }
                }
              }
            },
            ifRight = { contactInformation ->
              Snapshot.withMutableSnapshot {
                updateStateWithFetchedContactInformation(contactInformation)
                submittingData = null
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
          val trimmedPhoneNumber = PhoneNumber
            .fromStringAfterTrimmingWhitespaces(phoneNumber.text.toString())
            .getOrElse {
              return@CollectEvents
            }
          val emailValue = Email.fromString(email.text.toString()).getOrElse {
            return@CollectEvents
          }
          submittingData = trimmedPhoneNumber to emailValue
        }
      }
    }

    return when (dataFetchingState) {
      Error -> ContactInfoUiState.Error
      Fetching -> ContactInfoUiState.Loading
      Idle -> ContactInfoUiState.Content(
        emailState = email,
        phoneNumberState = phoneNumber,
        uploadedEmail = uploadedEmail,
        uploadedPhoneNumber = uploadedPhoneNumber,
        submittingUpdatedInfo = submittingData != null,
      )
    }
  }
}

private enum class DataFetchingState {
  Idle,
  Fetching,
  Error,
}
