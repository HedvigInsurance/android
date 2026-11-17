package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.core.common.android.validation.isValidEmail
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class TravelCertificateDateInputViewModel(
  @Assisted contractId: String?,
  getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  clock: Clock,
) : MoleculeViewModel<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState>(
    initialState = TravelCertificateDateInputUiState.Loading,
    presenter = TravelCertificateDateInputPresenter(
      contractId,
      getTravelCertificateSpecificationsUseCase,
      createTravelCertificateUseCase,
      clock,
    ),
    sharingStarted = SharingStarted.WhileSubscribed(5.seconds),
  )

internal class TravelCertificateDateInputPresenter(
  private val contractId: String?,
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val clock: Clock,
) : MoleculePresenter<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateDateInputEvent>.present(
    lastState: TravelCertificateDateInputUiState,
  ): TravelCertificateDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var requestToCreate by remember { mutableStateOf<CreateRequest?>(null) }
    var screenState by remember {
      mutableStateOf(
        when (lastState) {
          // A create or a navigation in flight belongs to the stopped run, so neither is restored. The create may
          // have reached the server, so it is shown as failed rather than as a form nobody submitted.
          is TravelCertificateDateInputUiState.Success -> lastState.copy(
            emailError = null,
            isCreating = false,
            createFailed = lastState.createFailed || lastState.isCreating,
            next = null,
          )

          else -> lastState
        },
      )
    }
    var emailRevision by remember {
      mutableIntStateOf((lastState as? TravelCertificateDateInputUiState.Success)?.emailRevision ?: 0)
    }
    // The `next` of the state this presenter last returned. Submit waits until a cleared `next` has been returned, so
    // an observer keyed on `next` always sees null between two equal steps.
    var returnedNext by remember { mutableStateOf<TravelCertificateTravellersStep?>(null) }

    CollectEvents { event ->
      val content = screenState as? TravelCertificateDateInputUiState.Success
      when (event) {
        TravelCertificateDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateDateInputEvent.ChangeEmailInput -> {
          if (content == null) return@CollectEvents
          screenState = content.copy(email = event.email, emailError = null)
        }

        is TravelCertificateDateInputEvent.SelectTravelDate -> {
          if (content == null || event.date !in content.minDate..content.maxDate) return@CollectEvents
          screenState = content.copy(pickedDate = event.date, selectedDate = event.date)
        }

        TravelCertificateDateInputEvent.StepShown -> {
          if (content == null) return@CollectEvents
          screenState = content.copy(next = null)
        }

        TravelCertificateDateInputEvent.Submit -> {
          if (content == null || content.isCreating || content.next != null || returnedNext != null) {
            return@CollectEvents
          }
          val email = content.email.trim()
          val emailError = when {
            email.isEmpty() -> EmailError.Empty
            !isValidEmail(email) -> EmailError.Invalid
            else -> null
          }
          if (emailError != null) {
            screenState = content.copy(emailError = emailError)
            return@CollectEvents
          }
          val travelDate = content.travelDate(clock)
          if (travelDate !in content.minDate..content.maxDate) {
            screenState = TravelCertificateDateInputUiState.Failure
            return@CollectEvents
          }
          val step = TravelCertificateTravellersStep(
            email = email,
            travelDate = travelDate,
            contractId = content.contractId,
          )
          if (content.hasCoInsured) {
            screenState = content.copy(emailError = null, createFailed = false, next = step)
          } else {
            screenState = content.copy(emailError = null, createFailed = false, isCreating = true)
            requestToCreate = CreateRequest(attempt = (requestToCreate?.attempt ?: 0) + 1, step = step)
          }
        }
      }
    }

    LaunchedEffect(requestToCreate) {
      val step = requestToCreate?.step ?: return@LaunchedEffect
      createTravelCertificateUseCase.invoke(
        contractId = step.contractId,
        startDate = step.travelDate,
        isMemberIncluded = true,
        coInsured = listOf(),
        email = step.email,
      ).fold(
        ifLeft = {
          val content = screenState as? TravelCertificateDateInputUiState.Success
          if (content != null) {
            screenState = content.copy(isCreating = false, createFailed = true)
          }
        },
        ifRight = { url ->
          screenState = TravelCertificateDateInputUiState.Created(url)
        },
      )
    }

    LaunchedEffect(loadIteration) {
      if (screenState is TravelCertificateDateInputUiState.Success ||
        screenState is TravelCertificateDateInputUiState.Created
      ) {
        return@LaunchedEffect
      }
      screenState = TravelCertificateDateInputUiState.Loading
      getTravelCertificateSpecificationsUseCase
        .invoke(contractId)
        .fold(
          ifLeft = { _ ->
            screenState = TravelCertificateDateInputUiState.Failure
          },
          ifRight = { travelCertificateData ->
            val specification = travelCertificateData.travelCertificateSpecification
            val minDate = specification.dateRange.start
            val maxDate = specification.dateRange.endInclusive
            emailRevision++
            screenState = TravelCertificateDateInputUiState.Success(
              contractId = specification.contractId,
              email = specification.email,
              emailRevision = emailRevision,
              emailError = null,
              minDate = minDate,
              maxDate = maxDate,
              pickedDate = null,
              selectedDate = defaultTravelDate(clock, minDate, maxDate),
              daysValid = specification.maxDurationDays,
              hasCoInsured = specification.numberOfCoInsured > 0,
              isCreating = false,
              createFailed = false,
              next = null,
            )
          },
        )
    }
    val state = when (val current = screenState) {
      is TravelCertificateDateInputUiState.Success -> current.copy(selectedDate = current.travelDate(clock))
      else -> current
    }
    SideEffect { returnedNext = (state as? TravelCertificateDateInputUiState.Success)?.next }
    return state
  }
}

/**
 * [attempt] increases with every accepted create, so a retry equal to the failed request still restarts the effect
 * keyed on it, even when both are handled before the next composition.
 */
private data class CreateRequest(val attempt: Int, val step: TravelCertificateTravellersStep)

private fun TravelCertificateDateInputUiState.Success.travelDate(clock: Clock): LocalDate =
  pickedDate ?: defaultTravelDate(clock, minDate, maxDate)

/** Today in the device zone, moved into the range. */
private fun defaultTravelDate(clock: Clock, minDate: LocalDate, maxDate: LocalDate): LocalDate =
  clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.coerceAtMost(maxDate).coerceAtLeast(minDate)

internal sealed interface TravelCertificateDateInputEvent {
  data object RetryLoadData : TravelCertificateDateInputEvent

  data class ChangeEmailInput(val email: String) : TravelCertificateDateInputEvent

  /** Ignored when [date] is outside the content's `minDate..maxDate`. */
  data class SelectTravelDate(val date: LocalDate) : TravelCertificateDateInputEvent

  /** Sent once the step in `next` is on screen, so a later Continue can open it again. */
  data object StepShown : TravelCertificateDateInputEvent

  /** Ignored while creating, and while a step is in `next` or its clearing has not been emitted yet. */
  data object Submit : TravelCertificateDateInputEvent
}

internal enum class EmailError {
  Empty,
  Invalid,
}

internal sealed interface TravelCertificateDateInputUiState {
  data object Loading : TravelCertificateDateInputUiState

  data object Failure : TravelCertificateDateInputUiState

  /**
   * [email] is the text as last typed. [emailRevision] increases only when the presenter writes [email] itself, so a
   * screen that owns its editing state copies [email] in only when the revision changes.
   */
  data class Success(
    val contractId: String,
    val email: String,
    val emailRevision: Int,
    val emailError: EmailError?,
    val minDate: LocalDate,
    val maxDate: LocalDate,
    /** The member's own pick, or null while the date follows today. */
    val pickedDate: LocalDate?,
    /** [pickedDate], or today moved into the range, as of when this state was emitted. Submit resolves it again. */
    val selectedDate: LocalDate,
    val daysValid: Int,
    val hasCoInsured: Boolean,
    /** A create is only observed while this state is collected; a restart shows it as [createFailed]. */
    val isCreating: Boolean,
    val createFailed: Boolean,
    val next: TravelCertificateTravellersStep?,
  ) : TravelCertificateDateInputUiState

  /** The certificate exists; nothing follows on this screen. */
  data class Created(val url: TravelCertificateUrl) : TravelCertificateDateInputUiState
}
