package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import com.hedvig.android.feature.greencard.navigation.GreenCardAddressInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardTripInput
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class GreenCardDateInputViewModel(
  @Assisted contractId: String,
  getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  languageService: LanguageService,
  backstack: Backstack,
) : MoleculeViewModel<GreenCardDateInputEvent, GreenCardDateInputUiState>(
    initialState = GreenCardDateInputUiState.Loading,
    presenter = GreenCardDateInputPresenter(
      contractId,
      getGreenCardSpecificationsUseCase,
      languageService,
      backstack,
    ),
  )

internal class GreenCardDateInputPresenter(
  private val contractId: String,
  private val getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  private val languageService: LanguageService,
  private val backstack: Backstack,
) : MoleculePresenter<GreenCardDateInputEvent, GreenCardDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<GreenCardDateInputEvent>.present(
    lastState: GreenCardDateInputUiState,
  ): GreenCardDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var form by remember {
      mutableStateOf((lastState as? GreenCardDateInputUiState.Success)?.form)
    }
    var loadFailed by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<DateError?>(null) }

    CollectEvents { event ->
      when (event) {
        GreenCardDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        GreenCardDateInputEvent.ClearDateError -> {
          dateError = null
        }

        GreenCardDateInputEvent.Submit -> {
          val currentForm = form ?: return@CollectEvents
          val startDate = currentForm.startDatePickerState.selectedDate()
          val endDate = currentForm.endDatePickerState.selectedDate()
          dateError = when {
            startDate == null -> DateError.MissingStartDate
            endDate == null -> DateError.MissingEndDate
            endDate < startDate -> DateError.EndBeforeStart
            else -> null
          }
          if (dateError == null) {
            val specification = currentForm.specification
            backstack.add(
              GreenCardAddressInputKey(
                GreenCardTripInput(
                  contractId = specification.contractId,
                  registrationNumber = specification.registrationNumber,
                  travelStartDate = startDate!!,
                  travelEndDate = endDate!!,
                ),
              ),
            )
          }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (form != null) return@LaunchedEffect
      loadFailed = false
      getGreenCardSpecificationsUseCase.invoke().fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Could not load green card specifications: $it" }
          loadFailed = true
        },
        ifRight = { specifications ->
          val specification = specifications.contracts.firstOrNull { it.contractId == contractId }
          if (specification == null) {
            logcat(LogPriority.ERROR) { "No green card specification for contract $contractId" }
            loadFailed = true
          } else {
            form = createForm(specification)
          }
        },
      )
    }

    val currentForm = form
    return when {
      currentForm != null -> GreenCardDateInputUiState.Success(
        form = currentForm,
        dateError = dateError,
      )

      loadFailed -> GreenCardDateInputUiState.Failure

      else -> GreenCardDateInputUiState.Loading
    }
  }

  private fun createForm(specification: GreenCardContractSpecification): GreenCardForm {
    val dateRange = specification.minStartDate..specification.maxEndDate
    val yearRange = dateRange.start.year..dateRange.endInclusive.year
    val startDatePickerState = HedvigDatePickerState(
      locale = languageService.getLocale(),
      initialSelectedDateMillis = specification.minStartDate.toUtcMillis(),
      initialDisplayedMonthMillis = null,
      yearRange = yearRange,
      selectableDates = selectableDatesIn(yearRange) { it in dateRange },
    )
    val endDatePickerState = HedvigDatePickerState(
      locale = languageService.getLocale(),
      initialSelectedDateMillis = null,
      initialDisplayedMonthMillis = null,
      yearRange = yearRange,
      selectableDates = selectableDatesIn(yearRange) { date ->
        val earliestEndDate = startDatePickerState.selectedDate() ?: specification.minStartDate
        date in earliestEndDate..specification.maxEndDate
      },
    )
    return GreenCardForm(
      specification = specification,
      startDatePickerState = startDatePickerState,
      endDatePickerState = endDatePickerState,
    )
  }
}

private fun selectableDatesIn(yearRange: IntRange, isSelectable: (LocalDate) -> Boolean) =
  object : HedvigSelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectable(utcTimeMillis.toUtcLocalDate())

    override fun isSelectableYear(year: Int): Boolean = year in yearRange
  }

internal fun HedvigDatePickerState.selectedDate(): LocalDate? = selectedDateMillis?.toUtcLocalDate()

private fun Long.toUtcLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

internal data class GreenCardForm(
  val specification: GreenCardContractSpecification,
  val startDatePickerState: HedvigDatePickerState,
  val endDatePickerState: HedvigDatePickerState,
)

internal enum class DateError { MissingStartDate, MissingEndDate, EndBeforeStart }

internal sealed interface GreenCardDateInputEvent {
  data object RetryLoadData : GreenCardDateInputEvent

  data object ClearDateError : GreenCardDateInputEvent

  data object Submit : GreenCardDateInputEvent
}

internal sealed interface GreenCardDateInputUiState {
  data object Loading : GreenCardDateInputUiState

  data object Failure : GreenCardDateInputUiState

  data class Success(
    val form: GreenCardForm,
    val dateError: DateError?,
  ) : GreenCardDateInputUiState
}
