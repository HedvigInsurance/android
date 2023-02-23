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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
internal class CancelInsuranceViewModel(
  private val insuranceId: InsuranceId,
  private val cancelInsuranceUseCase: CancelInsuranceUseCase,
  clock: Clock = Clock.System,
) : ViewModel() {
  private val dateSubmissionError = MutableStateFlow(false)
  private val dateSubmissionSuccess = MutableStateFlow(false)
  private val isPerformingCancelInsuranceNetworkRequest = MutableStateFlow(false)

  private val datePickerConfiguration = DatePickerConfiguration(clock)
  val dateValidator = datePickerConfiguration.dateValidator

  val uiState: StateFlow<CancelInsuranceUiState> = combine(
    dateSubmissionError,
    dateSubmissionSuccess,
    isPerformingCancelInsuranceNetworkRequest,
  ) { dateSubmissionError, dateSubmissionSuccess, isPerformingCancelInsuranceNetworkRequest ->
    CancelInsuranceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      dateSubmissionError = dateSubmissionError,
      dateSubmissionSuccess = dateSubmissionSuccess,
      isLoading = isPerformingCancelInsuranceNetworkRequest,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    CancelInsuranceUiState(
      datePickerConfiguration.datePickerState,
      dateSubmissionError.value,
      dateSubmissionSuccess.value,
      isPerformingCancelInsuranceNetworkRequest.value,
    ),
  )

  fun showedError() {
    dateSubmissionError.update { false }
  }

  fun submitSelectedDate() {
    val viewState = uiState.value
    if (!viewState.canSubmitSelectedDate()) return
    val selectedDateMillis = viewState.datePickerState.selectedDateMillis ?: return
    isPerformingCancelInsuranceNetworkRequest.update { true }
    viewModelScope.launch {
      val result = cancelInsuranceUseCase.invoke(
        insuranceId,
        selectedDateMillis,
      )
      when (result) {
        is Either.Left -> dateSubmissionError.update { true }
        is Either.Right -> dateSubmissionSuccess.update { true }
      }
      isPerformingCancelInsuranceNetworkRequest.update { false }
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
