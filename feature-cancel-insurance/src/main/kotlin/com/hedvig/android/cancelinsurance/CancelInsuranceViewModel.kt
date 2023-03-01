package com.hedvig.android.cancelinsurance

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.cancelinsurance.data.CancelInsuranceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
internal class CancelInsuranceViewModel(
  private val insuranceId: InsuranceId,
  private val cancelInsuranceUseCase: CancelInsuranceUseCase,
  clock: Clock = Clock.System,
) : ViewModel() {
  private val datePickerConfiguration = DatePickerConfiguration(clock)
  val dateValidator = datePickerConfiguration.dateValidator

  private val _uiState: MutableStateFlow<CancelInsuranceUiState> = MutableStateFlow(
    CancelInsuranceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      dateSubmissionError = false,
      dateSubmissionSuccess = false,
      isLoading = false,
    ),
  )
  val uiState = _uiState.asStateFlow()

  fun showedError() {
    _uiState.update {
      it.copy(dateSubmissionError = false)
    }
  }

  fun submitSelectedDate() {
    val uiState = _uiState.value
    if (!uiState.canSubmitSelectedDate()) return
    val selectedDateMillis = uiState.datePickerState.selectedDateMillis ?: return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val result = cancelInsuranceUseCase.invoke(
        insuranceId,
        selectedDateMillis,
      )
      when (result) {
        is Either.Left -> _uiState.update {
          it.copy(
            dateSubmissionError = true,
            isLoading = false,
          )
        }
        is Either.Right -> _uiState.update {
          it.copy(
            dateSubmissionSuccess = true,
            isLoading = false,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
class DatePickerConfiguration(clock: Clock) {
  private val todayAtStartOfDay: LocalDateTime = clock.now()
    .toLocalDateTime(TimeZone.UTC)
    .date
    .atStartOfDayIn(TimeZone.UTC)
    .toLocalDateTime(TimeZone.UTC)
  private val todayAtStartOfDayEpochMillis = todayAtStartOfDay.toInstant(TimeZone.UTC).toEpochMilliseconds()
  private val yearsRange = todayAtStartOfDay.year..2100

  val datePickerState = DatePickerState(null, null, yearsRange)
  val dateValidator: (Long) -> Boolean = { selectedDateEpochMillis ->
    selectedDateEpochMillis >= todayAtStartOfDayEpochMillis
  }
}

@OptIn(ExperimentalMaterial3Api::class)
data class CancelInsuranceUiState(
  val datePickerState: DatePickerState,
  val dateSubmissionError: Boolean,
  val dateSubmissionSuccess: Boolean,
  val isLoading: Boolean,
) {
  val canContinue: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      dateSubmissionError,
      dateSubmissionSuccess,
      isLoading,
    ) { canSubmitSelectedDate() }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun CancelInsuranceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null &&
    !dateSubmissionError &&
    !dateSubmissionSuccess &&
    !isLoading
}
