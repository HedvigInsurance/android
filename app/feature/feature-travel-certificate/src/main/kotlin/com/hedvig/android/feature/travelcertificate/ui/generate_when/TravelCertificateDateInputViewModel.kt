package com.hedvig.android.feature.travelcertificate.ui.generate_when

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

internal class TravelCertificateDateInputViewModel(
  contractId: String?,
  getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : MoleculeViewModel<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState>(
    initialState = TravelCertificateDateInputUiState.Loading,
    presenter = TravelCertificateDateInputPresenter(
      contractId,
      getTravelCertificateSpecificationsUseCase,
      createTravelCertificateUseCase,
    ),
  )

internal class TravelCertificateDateInputPresenter(
  private val contractId: String?,
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : MoleculePresenter<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateDateInputEvent>.present(
    lastState: TravelCertificateDateInputUiState,
  ): TravelCertificateDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }

    var generateIteration by remember { mutableIntStateOf(0) }

    var screenContent by remember { mutableStateOf<DateInputScreenContent>(DateInputScreenContent.Loading) }

    var travelDate by remember {
      mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
      )
    }

    var email by remember { mutableStateOf<ValidatedInput<String?>>(ValidatedInput(null)) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateDateInputEvent.ChangeEmailInput -> {
          email = ValidatedInput(event.email)
        }

        is TravelCertificateDateInputEvent.ChangeDataInput -> {
          travelDate = event.localDate
        }

        TravelCertificateDateInputEvent.GenerateTravelCertificate -> {
          generateIteration++
        }
      }
    }

    LaunchedEffect(generateIteration) {
      val currentContent = screenContent
      val currentEmail = email.input
      if (currentContent is DateInputScreenContent.Success && currentEmail != null) {
        screenContent = DateInputScreenContent.Loading
        createTravelCertificateUseCase.invoke(
          contractId = currentContent.details.contractId,
          startDate = travelDate,
          isMemberIncluded = true,
          coInsured = listOf(),
          email = currentEmail,
        ).fold(
          ifLeft = { _ ->
            screenContent = DateInputScreenContent.Failure
            // todo: so on Retry we would just show the initial DateInput screen again
          },
          ifRight = { url ->
            screenContent = DateInputScreenContent.UrlFetched(url)
          },
        )
      }
    }

    LaunchedEffect(loadIteration) {
      screenContent = DateInputScreenContent.Loading
      getTravelCertificateSpecificationsUseCase
        .invoke(contractId)
        .fold(
          ifLeft = { _ ->
            // todo: so here we have 2 types of TravelCertificateError (Error and NotEligible),
            // todo: but the second one shouldn't really appear in any case.
            // todo: could probably change Either.Left type from TravelCertificateError to the usual ErrorMessage?
            screenContent = DateInputScreenContent.Failure
          },
          ifRight = { travelCertificateData ->
            val travelSpecification = travelCertificateData.travelCertificateSpecification
            val datePickerState = DatePickerState(
              initialSelectedDateMillis = null,
              initialDisplayedMonthMillis = null,
              yearRange = travelSpecification.dateRange.start.year..travelSpecification.dateRange.endInclusive.year,
              initialDisplayMode = DisplayMode.Picker,
            )
            email = ValidatedInput(travelSpecification.email)
            screenContent = DateInputScreenContent.Success(
              SpecificationsDetails(
                contractId = travelSpecification.contractId,
                hasCoEnsured = travelSpecification.numberOfCoInsured > 0,
                datePickerState = datePickerState,
                dateValidator = { date ->
                  val selectedDate =
                    Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()).date
                  travelSpecification.dateRange.contains(selectedDate)
                },
                daysValid = travelSpecification.maxDurationDays,
              ),
            )
          },
        )
    }
    return when (val currentContent = screenContent) {
      DateInputScreenContent.Failure -> TravelCertificateDateInputUiState.Failure
      DateInputScreenContent.Loading -> TravelCertificateDateInputUiState.Loading
      is DateInputScreenContent.Success -> {
        TravelCertificateDateInputUiState.Success(
          email = email,
          travelDate = travelDate,
          contractId = currentContent.details.contractId,
          hasCoEnsured = currentContent.details.hasCoEnsured,
          datePickerState = currentContent.details.datePickerState,
          dateValidator = currentContent.details.dateValidator,
          daysValid = currentContent.details.daysValid,
        )
      }

      is DateInputScreenContent.UrlFetched -> {
        TravelCertificateDateInputUiState.UrlFetched(currentContent.travelCertificateUrl)
      }
    }
  }
}

private sealed interface DateInputScreenContent {
  data object Loading : DateInputScreenContent

  data object Failure : DateInputScreenContent

  data class Success(val details: SpecificationsDetails) : DateInputScreenContent

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : DateInputScreenContent
}

private data class SpecificationsDetails(
  val contractId: String,
  val datePickerState: DatePickerState,
  val dateValidator: (Long) -> Boolean,
  val daysValid: Int,
  val hasCoEnsured: Boolean,
)

internal sealed interface TravelCertificateDateInputEvent {
  data object RetryLoadData : TravelCertificateDateInputEvent

  data class ChangeEmailInput(val email: String?) : TravelCertificateDateInputEvent

  data class ChangeDataInput(val localDate: LocalDate) : TravelCertificateDateInputEvent

  data object GenerateTravelCertificate : TravelCertificateDateInputEvent
}

internal sealed interface TravelCertificateDateInputUiState {
  data object Loading : TravelCertificateDateInputUiState

  data object Failure : TravelCertificateDateInputUiState

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateDateInputUiState

  data class Success(
    val contractId: String,
    val email: ValidatedInput<String?>,
    val travelDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val hasCoEnsured: Boolean,
    val datePickerState: DatePickerState,
    val dateValidator: (Long) -> Boolean,
    val daysValid: Int,
  ) : TravelCertificateDateInputUiState {
    val isInputValid: Boolean
      get() {
        return email.errorMessageRes == null
      }

    fun validateInput(): Success {
      return copy(
        email = email.copy(
          errorMessageRes = if (!email.isPresent || email.input?.isBlank() == true) {
            hedvig.resources.R.string.travel_certificate_email_empty_error
          } else {
            null
          },
        ),
      )
    }
  }
}

@Serializable
internal data class TravelCertificatePrimaryInput(
  val email: String,
  val travelDate: LocalDate,
  val contractId: String,
)
