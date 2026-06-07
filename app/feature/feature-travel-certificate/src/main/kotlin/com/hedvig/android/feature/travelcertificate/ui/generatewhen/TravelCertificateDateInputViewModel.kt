package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.core.common.android.validation.validateEmail
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import hedvig.resources.PROFILE_MY_INFO_INVALID_EMAIL
import hedvig.resources.Res
import hedvig.resources.travel_certificate_email_empty_error
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource

@AssistedInject
internal class TravelCertificateDateInputViewModel(
  @Assisted contractId: String?,
  getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  languageService: LanguageService,
  backstack: Backstack,
) : MoleculeViewModel<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState>(
    initialState = TravelCertificateDateInputUiState.Loading,
    presenter = TravelCertificateDateInputPresenter(
      contractId,
      getTravelCertificateSpecificationsUseCase,
      createTravelCertificateUseCase,
      languageService,
      backstack,
    ),
    sharingStarted = SharingStarted.WhileSubscribed(5.seconds),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted contractId: String?,
    ): TravelCertificateDateInputViewModel
  }
}

internal class TravelCertificateDateInputPresenter(
  private val contractId: String?,
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val languageService: LanguageService,
  private val backstack: Backstack,
) : MoleculePresenter<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateDateInputEvent>.present(
    lastState: TravelCertificateDateInputUiState,
  ): TravelCertificateDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }

    var createTravelCertificateData by remember { mutableStateOf<CreateTravelCertificateData?>(null) }

    var screenContent by remember {
      mutableStateOf(
        when (lastState) {
          is TravelCertificateDateInputUiState.Success -> {
            DateInputScreenContent.Success(
              DateInputScreenContent.Success.SpecificationsDetails(
                contractId = lastState.contractId,
                email = lastState.email,
                datePickerState = lastState.datePickerState,
                daysValid = lastState.daysValid,
                hasCoInsured = lastState.hasCoInsured,
              ),
            )
          }

          TravelCertificateDateInputUiState.Failure -> {
            DateInputScreenContent.Failure
          }

          TravelCertificateDateInputUiState.Loading -> {
            DateInputScreenContent.Loading
          }
        },
      )
    }

    var invalidEmailErrorMessage by remember { mutableStateOf<StringResource?>(null) }

    CollectEvents { event ->

      fun validateInputAndContinue() {
        val successScreenContent = screenContent as? DateInputScreenContent.Success ?: return
        if (successScreenContent.details.email != null &&
          validateEmail(
            successScreenContent.details.email,
          ).isSuccessful
        ) {
          if (successScreenContent.details.hasCoInsured) {
            backstack.add(
              TravelCertificateTravellersInputKey(
                TravelCertificateTravellersInputKey.TravelCertificatePrimaryInput(
                  successScreenContent.details.email,
                  successScreenContent.details.travelDate,
                  successScreenContent.details.contractId,
                ),
              ),
            )
          } else {
            createTravelCertificateData = CreateTravelCertificateData(
              successScreenContent.details.contractId,
              successScreenContent.details.travelDate,
              successScreenContent.details.email,
            )
          }
          invalidEmailErrorMessage = null
        } else {
          invalidEmailErrorMessage = if (successScreenContent.details.email.isNullOrEmpty()) {
            Res.string.travel_certificate_email_empty_error
          } else {
            Res.string.PROFILE_MY_INFO_INVALID_EMAIL
          }
        }
      }

      when (event) {
        is TravelCertificateDateInputEvent.ChangeEmailInput -> {
          val successScreenContent = screenContent as? DateInputScreenContent.Success ?: return@CollectEvents
          invalidEmailErrorMessage = null
          screenContent = successScreenContent.copy(
            details = successScreenContent.details.copy(
              email = event.email,
            ),
          )
        }

        TravelCertificateDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateDateInputEvent.Submit -> {
          validateInputAndContinue()
        }
      }
    }

    LaunchedEffect(createTravelCertificateData) {
      val currentCreateTravelCertificateData = createTravelCertificateData ?: return@LaunchedEffect
      screenContent = DateInputScreenContent.Loading
      createTravelCertificateUseCase.invoke(
        contractId = currentCreateTravelCertificateData.contractId,
        startDate = currentCreateTravelCertificateData.travelDate,
        isMemberIncluded = true,
        coInsured = listOf(),
        email = currentCreateTravelCertificateData.email,
      ).fold(
        ifLeft = {
          screenContent = DateInputScreenContent.Failure
        },
        ifRight = { url ->
          backstack.navigateAndPopUpTo<TravelCertificateKey>(ShowCertificateKey(url), inclusive = false)
        },
      )
      createTravelCertificateData = null
    }

    LaunchedEffect(loadIteration) {
      if (screenContent is DateInputScreenContent.Success) {
        return@LaunchedEffect
      }
      screenContent = DateInputScreenContent.Loading
      getTravelCertificateSpecificationsUseCase
        .invoke(contractId)
        .fold(
          ifLeft = { _ ->
            screenContent = DateInputScreenContent.Failure
          },
          ifRight = { travelCertificateData ->
            val travelSpecification = travelCertificateData.travelCertificateSpecification
            val yearRange = travelSpecification.dateRange.start.year..travelSpecification.dateRange.endInclusive.year
            val datePickerState = HedvigDatePickerState(
              locale = languageService.getLocale(),
              initialSelectedDateMillis = null,
              initialDisplayedMonthMillis = null,
              yearRange = yearRange,
              selectableDates = object : HedvigSelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                  val selectedDate =
                    Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date
                  return selectedDate in travelSpecification.dateRange
                }

                override fun isSelectableYear(year: Int): Boolean = year in yearRange
              },
            )
            screenContent = DateInputScreenContent.Success(
              DateInputScreenContent.Success.SpecificationsDetails(
                datePickerState = datePickerState,
                daysValid = travelSpecification.maxDurationDays,
                email = travelSpecification.email,
                contractId = travelSpecification.contractId,
                hasCoInsured = travelSpecification.numberOfCoInsured > 0,
              ),
            )
          },
        )
    }
    return when (val currentContent = screenContent) {
      DateInputScreenContent.Failure -> {
        TravelCertificateDateInputUiState.Failure
      }

      DateInputScreenContent.Loading -> {
        TravelCertificateDateInputUiState.Loading
      }

      is DateInputScreenContent.Success -> {
        TravelCertificateDateInputUiState.Success(
          email = currentContent.details.email,
          travelDate = currentContent.details.travelDate,
          contractId = currentContent.details.contractId,
          hasCoInsured = currentContent.details.hasCoInsured,
          datePickerState = currentContent.details.datePickerState,
          daysValid = currentContent.details.daysValid,
          errorMessageRes = invalidEmailErrorMessage,
        )
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
      val email: String?,
      val datePickerState: HedvigDatePickerState,
      val daysValid: Int,
      val hasCoInsured: Boolean,
    ) {
      val travelDate: LocalDate
        get() = datePickerState.selectedDateMillis?.let {
          Instant.fromEpochMilliseconds(it)
            .toLocalDateTime(TimeZone.UTC)
            .date
        } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
  }
}

internal sealed interface TravelCertificateDateInputEvent {
  data object RetryLoadData : TravelCertificateDateInputEvent

  data class ChangeEmailInput(val email: String) : TravelCertificateDateInputEvent

  data object Submit : TravelCertificateDateInputEvent
}

internal sealed interface TravelCertificateDateInputUiState {
  data object Loading : TravelCertificateDateInputUiState

  data object Failure : TravelCertificateDateInputUiState

  data class Success(
    val contractId: String,
    val email: String?,
    val travelDate: LocalDate,
    val hasCoInsured: Boolean,
    val datePickerState: HedvigDatePickerState,
    val daysValid: Int,
    val errorMessageRes: StringResource?,
  ) : TravelCertificateDateInputUiState
}

private data class CreateTravelCertificateData(
  val contractId: String,
  val travelDate: LocalDate,
  val email: String,
)
