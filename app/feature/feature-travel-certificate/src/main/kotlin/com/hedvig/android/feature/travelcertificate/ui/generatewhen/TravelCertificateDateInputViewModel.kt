package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    sharingStarted = SharingStarted.WhileSubscribed(5.seconds),
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
        Clock.System.now()
          .toLocalDateTime(TimeZone.currentSystemDefault()).date,
      )
    }

    var contractIdFromBackend by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf<String?>(null) }

    var primaryInput by remember {
      mutableStateOf<TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput?>(null)
    }

    var errorMessage by remember { mutableStateOf<Int?>(null) }

    var hasCoInsured by remember { mutableStateOf(false) }

    CollectEvents { event ->

      fun generateUrl(email: String?) {
        val currentEmail = email
        val isCurrentInputValid = currentEmail != null && validateEmail(currentEmail).isSuccessful
        if (isCurrentInputValid) {
          if (hasCoInsured) {
            val currentContractId = contractIdFromBackend
            val travelCertificatePrimaryInput = if (currentEmail != null &&
              currentContractId != null
            ) {
              TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput(
                currentEmail,
                travelDate,
                currentContractId,
              )
            } else {
              null
            }
            if (travelCertificatePrimaryInput == null) {
              logcat(LogPriority.INFO) {
                "TravelCertificateDateInputPresenter: currentEmail or currentContractId are null when they shouldn't be"
              }
            }
            primaryInput = travelCertificatePrimaryInput
          } else {
            generateIteration++ //
          }
          errorMessage = null
        } else {
          errorMessage = if (currentEmail.isNullOrEmpty()) {
            R.string.travel_certificate_email_empty_error
          } else {
            R.string.PROFILE_MY_INFO_INVALID_EMAIL
          }
        }
      }

      when (event) {
        TravelCertificateDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateDateInputEvent.ChangeDataInput -> {
          travelDate = event.localDate
        }

        TravelCertificateDateInputEvent.NullifyPrimaryInput -> {
          primaryInput = null
        }

        is TravelCertificateDateInputEvent.ValidateInputAndChooseDirection -> {
          email = event.emailInput
          generateUrl(email)
        }
      }
    }

    LaunchedEffect(generateIteration) {
      val currentContent = screenContent
      val currentEmail = email
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
            email = travelSpecification.email
            contractIdFromBackend = travelSpecification.contractId
            hasCoInsured = travelSpecification.numberOfCoInsured > 0
            screenContent = DateInputScreenContent.Success(
              DateInputScreenContent.Success.SpecificationsDetails(
                contractId = travelSpecification.contractId,
                hasCoInsured = hasCoInsured,
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
          hasCoInsured = currentContent.details.hasCoInsured,
          datePickerState = currentContent.details.datePickerState,
          dateValidator = currentContent.details.dateValidator,
          daysValid = currentContent.details.daysValid,
          primaryInput = primaryInput,
          errorMessageRes = errorMessage,
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

  data class Success(val details: SpecificationsDetails) : DateInputScreenContent {
    data class SpecificationsDetails(
      val contractId: String,
      val datePickerState: DatePickerState,
      val dateValidator: (Long) -> Boolean,
      val daysValid: Int,
      val hasCoInsured: Boolean,
    )
  }

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : DateInputScreenContent
}

internal sealed interface TravelCertificateDateInputEvent {
  data object RetryLoadData : TravelCertificateDateInputEvent

  data class ChangeDataInput(val localDate: LocalDate) : TravelCertificateDateInputEvent

  data class ValidateInputAndChooseDirection(val emailInput: String) : TravelCertificateDateInputEvent

  data object NullifyPrimaryInput : TravelCertificateDateInputEvent
}

internal sealed interface TravelCertificateDateInputUiState {
  data object Loading : TravelCertificateDateInputUiState

  data object Failure : TravelCertificateDateInputUiState

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateDateInputUiState

  data class Success(
    val contractId: String,
    val email: String?,
    val travelDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val hasCoInsured: Boolean,
    val datePickerState: DatePickerState,
    val dateValidator: (Long) -> Boolean,
    val daysValid: Int,
    val primaryInput: TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput?,
    val errorMessageRes: Int?,
  ) : TravelCertificateDateInputUiState
}
