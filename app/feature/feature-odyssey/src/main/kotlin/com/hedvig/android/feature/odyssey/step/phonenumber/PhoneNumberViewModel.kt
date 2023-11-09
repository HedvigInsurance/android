package com.hedvig.android.feature.odyssey.step.phonenumber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PhoneNumberViewModel(
  initialPhoneNumber: String?,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(PhoneNumberUiState(initialPhoneNumber ?: ""))
  val uiState: StateFlow<PhoneNumberUiState> = _uiState.asStateFlow()

  fun updatePhoneNumber(phoneNumber: String) {
    _uiState.update { it.copy(phoneNumber = phoneNumber) }
  }

  fun submitPhoneNumber() {
    if (_uiState.value.status != PhoneNumberUiState.Status.IDLE) return
    val phoneNumber = _uiState.value.phoneNumber
    viewModelScope.launch {
      _uiState.update { it.copy(status = PhoneNumberUiState.Status.LOADING) }
      claimFlowRepository.submitPhoneNumber(phoneNumber).fold(
        ifLeft = {
          _uiState.update { it.copy(status = PhoneNumberUiState.Status.ERROR) }
        },
        ifRight = { claimFlowStep ->
          _uiState.update {
            it.copy(
              status = PhoneNumberUiState.Status.IDLE,
              nextStep = claimFlowStep,
            )
          }
        },
      )
    }
  }

  fun showedError() {
    _uiState.update {
      if (it.status == PhoneNumberUiState.Status.ERROR) {
        it.copy(status = PhoneNumberUiState.Status.IDLE)
      } else {
        it
      }
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class PhoneNumberUiState(
  val phoneNumber: String,
  val status: Status = Status.IDLE,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = status == Status.IDLE && nextStep == null

  enum class Status {
    IDLE,
    LOADING,
    ERROR,
  }
}
