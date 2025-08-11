package com.hedvig.android.feature.odyssey.step.dateofoccurrence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.language.LanguageService
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class DateOfOccurrenceViewModel(
  dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence,
  private val claimFlowRepository: ClaimFlowRepository,
  languageService: LanguageService,
) : ViewModel() {
  private val _uiState: MutableStateFlow<DateOfOccurrenceUiState> = MutableStateFlow(
    DateOfOccurrenceUiState(
      datePickerUiState = DatePickerUiState(
        locale = languageService.getLocale(),
        initiallySelectedDate = dateOfOccurrence.dateOfOccurrence,
        maxDate = dateOfOccurrence.maxDate,
      ),
      dateSubmissionError = false,
      nextStep = null,
      isLoading = false,
    ),
  )
  val uiState: StateFlow<DateOfOccurrenceUiState> = _uiState.asStateFlow()

  fun submitSelectedDate() {
    val uiState = _uiState.value
    if (!uiState.canSubmitSelectedDate()) return
    _uiState.update { it.copy(isLoading = true) }
    val selectedDateMillis: Long? = uiState.datePickerUiState.datePickerState.selectedDateMillis
    val selectedDate: LocalDate? = selectedDateMillis?.let {
      Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
    }
    viewModelScope.launch {
      claimFlowRepository.submitDateOfOccurrence(selectedDate).fold(
        ifLeft = {
          _uiState.update {
            it.copy(
              dateSubmissionError = true,
              isLoading = false,
            )
          }
        },
        ifRight = { claimFlowStep: ClaimFlowStep ->
          _uiState.update {
            it.copy(
              nextStep = claimFlowStep,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(dateSubmissionError = false)
    }
  }
}

internal data class DateOfOccurrenceUiState(
  val datePickerUiState: DatePickerUiState,
  val dateSubmissionError: Boolean,
  val nextStep: ClaimFlowStep?,
  val isLoading: Boolean,
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      dateSubmissionError,
      nextStep,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun DateOfOccurrenceUiState.canSubmitSelectedDate(): Boolean {
  return !dateSubmissionError &&
    nextStep == null &&
    !isLoading
}
