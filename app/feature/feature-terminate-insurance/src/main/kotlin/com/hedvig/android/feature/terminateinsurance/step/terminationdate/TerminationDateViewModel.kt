package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerImmutableState
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateParameters
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal sealed interface TerminationDateEvent {
  data class ChangeSelectedDate(val dateMillis: Long?) : TerminationDateEvent

  data object ToggleCheckBox : TerminationDateEvent
}

internal class TerminationDateViewModel(
  parameters: TerminationDateParameters,
  languageService: LanguageService,
) : MoleculeViewModel<TerminationDateEvent, TerminateInsuranceUiState>(
    initialState = TerminateInsuranceUiState(
      datePickerState = DatePickerConfiguration(
        languageService.getLocale(),
        parameters.minDate,
        parameters.maxDate,
      ).datePickerState,
      isLoading = false,
      exposureName = parameters.commonParams.exposureName,
      displayName = parameters.commonParams.insuranceDisplayName,
      isCheckBoxChecked = false,
    ),
    presenter = TerminationDatePresenter(),
  )

private class TerminationDatePresenter : MoleculePresenter<TerminationDateEvent, TerminateInsuranceUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationDateEvent>.present(
    lastState: TerminateInsuranceUiState,
  ): TerminateInsuranceUiState {
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        is TerminationDateEvent.ChangeSelectedDate -> {
          currentState = currentState.copy(
            datePickerState = currentState.datePickerState.copy(
              selectedDateMillis = event.dateMillis,
            ),
          )
        }

        TerminationDateEvent.ToggleCheckBox -> {
          currentState = currentState.copy(
            isCheckBoxChecked = !currentState.isCheckBoxChecked,
          )
        }
      }
    }

    return currentState
  }
}

private class DatePickerConfiguration(locale: Locale, minDate: LocalDate, maxDate: LocalDate) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = HedvigDatePickerImmutableState(
    locale = locale,
    selectedDateMillis = null,
    displayedMonthMillis = null,
    yearRange = yearRange,
    minDateInMillis = minDateInMillis,
    maxDateInMillis = maxDateInMillis,
  )
}

internal data class TerminateInsuranceUiState(
  val datePickerState: HedvigDatePickerImmutableState,
  val isLoading: Boolean,
  val exposureName: String,
  val displayName: String,
  val isCheckBoxChecked: Boolean,
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      isLoading,
      isCheckBoxChecked,
    ) { canSubmitSelectedDate() }
}

private fun TerminateInsuranceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null && !isLoading && isCheckBoxChecked
}
