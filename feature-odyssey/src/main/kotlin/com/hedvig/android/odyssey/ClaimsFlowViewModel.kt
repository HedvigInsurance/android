package com.hedvig.android.odyssey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClaimsFlowViewModel(
  private val itemType: String?,
  private val itemProblem: String?,
  private val claimsRepository: ClaimsFlowRepository,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState

  init {
    viewModelScope.launch {
      createClaim()
    }
  }

  suspend fun createClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    val result = claimsRepository.getOrCreateClaim(itemType, itemProblem)
    value = updateViewState(result)
  }

  suspend fun onNext() = with(_viewState) {
    val nextIndex = value.currentInputIndex + 1
    val nrOfInputs = value.inputs.size
    if (nextIndex < nrOfInputs) {
      update { it.copy(currentInputIndex = nextIndex) }
    } else {
      updateClaim()
      openClaim()
    }
  }

  private suspend fun updateClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    val result = claimsRepository.updateClaim(value.claimState)
    value = updateViewState(result)
  }

  private suspend fun openClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    val result = claimsRepository.openClaim()
    value = updateViewState(result)
  }

  suspend fun openClaimAndPayout(amount: MonetaryAmount) = with(_viewState) {
    update { it.copy(isLoadingPayment = true) }
    when (val result = claimsRepository.openClaim(amount)) {
      is ClaimResult.Error -> update { it.copy(errorMessage = result.message, isLoadingPayment = false) }
      is ClaimResult.Success -> update { it.copy(shouldExit = true, isLoadingPayment = false) }
    }
  }

  fun onBack() = with(_viewState) {
    val previousIndex = value.currentInputIndex - 1
    if (previousIndex >= 0) {
      update { it.copy(currentInputIndex = previousIndex) }
    } else {
      update { it.copy(shouldExit = true) }
    }
  }

  suspend fun onAudioFile(file: File) = with(_viewState) {
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

  fun onPurchasePrice(price: MonetaryAmount) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(purchasePrice = price)
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onTypeOfDamage(problem: AutomationClaimInputDTO2.SingleItem.ClaimProblem) = with(_viewState) {
    update {
      val newItem = it.claimState.item.copy(problemIds = listOf(problem))
      it.copy(claimState = it.claimState.copy(item = newItem))
    }
  }

  fun onExit() {
    _viewState.update { it.copy(shouldExit = true) }
  }

  fun onDismissError() {
    _viewState.update { it.copy(errorMessage = null) }
  }
}

private fun MutableStateFlow<ViewState>.updateViewState(claimResult: ClaimResult) = when (claimResult) {
  is ClaimResult.Error -> this.value.copy(errorMessage = claimResult.message, isLoading = false)
  is ClaimResult.Success -> this.value.copy(
    claimState = claimResult.claimState,
    inputs = claimResult.inputs,
    resolutions = claimResult.resolutions,
    isLoading = false,
  )
}
