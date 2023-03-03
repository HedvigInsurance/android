package com.hedvig.android.odyssey.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.android.odyssey.repository.ClaimResult
import com.hedvig.android.odyssey.repository.ClaimsFlowRepository
import com.hedvig.android.odyssey.repository.PhoneNumberRepository
import com.hedvig.android.odyssey.repository.PhoneNumberResult
import com.hedvig.odyssey.remote.file.File
import com.hedvig.odyssey.remote.money.MonetaryAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class InputViewModel(
  private val commonClaimId: String?,
  private val repository: ClaimsFlowRepository,
  private val getPhoneNumberUseCase: PhoneNumberRepository,
) : ViewModel() {

  private val _viewState = MutableStateFlow(InputViewState())
  val viewState = _viewState

  init {
    viewModelScope.launch {
      createClaim()
    }
  }

  private suspend fun createClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    value = when (val phoneNumberResult = getPhoneNumberUseCase.getPhoneNumber()) {
      is PhoneNumberResult.Error -> value.copy(errorMessage = phoneNumberResult.message, isLoading = false)
      is PhoneNumberResult.Success -> {
        val result = repository.getOrCreateClaim(commonClaimId)
        updateViewState(result).copy(phoneNumber = phoneNumberResult.phoneNumber)
      }
    }
  }

  fun onNext() = with(_viewState) {
    viewModelScope.launch {
      val nextIndex = value.currentInputIndex + 1
      val nrOfInputs = value.inputs.size
      if (nextIndex < nrOfInputs) {
        update { it.copy(currentInputIndex = nextIndex) }
      } else {
        updateClaim(nrOfInputs)
      }
    }
  }

  private suspend fun updateClaim(nrOfInputs: Int) = with(_viewState) {
    update { it.copy(isLoading = true) }
    val result = repository.updateClaim(value.claimState, nrOfInputs)
    value = updateViewState(result)
  }

  fun onBack() = with(_viewState) {
    val previousIndex = value.currentInputIndex - 1
    if (previousIndex >= 0) {
      update { it.copy(currentInputIndex = previousIndex) }
    } else {
      update { it.copy(shouldExit = true) }
    }
  }

  fun onAudioFile(file: File) = with(_viewState) {
    // TODO Upload audio file
    update { it.copy(claimState = value.claimState.copy(audioUrl = file.name)) }
  }

  fun onLocation(location: AutomationClaimDTO2.ClaimLocation) = with(_viewState) {
    update { it.copy(claimState = value.claimState.copy(location = location)) }
  }

  fun onDateOfOccurrence(date: LocalDate) = with(_viewState) {
    update { it.copy(claimState = value.claimState.copy(dateOfOccurrence = date)) }
  }

  fun onDateOfPurchase(date: LocalDate) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(purchaseDate = date)
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onPurchasePrice(price: MonetaryAmount?) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(purchasePrice = price)
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onTypeOfDamage(problem: AutomationClaimInputDTO2.SingleItem.ClaimProblem) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(selectedProblem = problem)
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onModelOption(modelOption: AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(selectedModelOption = modelOption)
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onPhoneNumber(phoneNumberInput: String) {
    _viewState.update { it.copy(phoneNumber = phoneNumberInput) }
  }

  fun updatePhoneNumber() {
    viewModelScope.launch {
      val result = getPhoneNumberUseCase.updatePhoneNumber(viewState.value.phoneNumber)
      if (result is OperationResult.Error) {
        _viewState.update { it.copy(errorMessage = result.message) }
      }
    }
  }

  fun onDismissError() {
    _viewState.update { it.copy(errorMessage = null) }
  }
}

private fun MutableStateFlow<InputViewState>.updateViewState(claimResult: ClaimResult) = when (claimResult) {
  is ClaimResult.Error -> value.copy(
    errorMessage = claimResult.message,
    isLoading = false,
  )
  is ClaimResult.Success -> value.copy(
    claimState = claimResult.claimState,
    inputs = claimResult.inputs,
    resolution = claimResult.resolution,
    isLoading = false,
  )
}
