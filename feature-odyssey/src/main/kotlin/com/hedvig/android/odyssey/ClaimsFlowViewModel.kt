package com.hedvig.android.odyssey

import androidx.lifecycle.ViewModel
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ClaimsFlowViewModel(
  private val itemType: String?,
  private val itemProblem: String?,
  private val claimsRepository: ClaimsFlowRepository,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState: StateFlow<ViewState> = _viewState

  suspend fun createClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    when (val result = claimsRepository.createOrRestartClaim(itemType, itemProblem)) {
      is ClaimResult.Error -> update { it.copy(errorMessage = result.message, isLoading = false) }
      is ClaimResult.Success -> update { it.copy(claim = result.claim, isLoading = false) }
    }
  }

  suspend fun updateClaim() = with(_viewState) {
    val claimState = value.claim?.state
    if (claimState != null) {
      update { it.copy(isLoading = true) }
      when (val result = claimsRepository.updateClaim(claimState)) {
        is ClaimResult.Error -> update { it.copy(errorMessage = result.message, isLoading = false) }
        is ClaimResult.Success -> update { it.copy(id = UUID.randomUUID(), claim = result.claim, isLoading = false) }
      }
    } else {
      update { it.copy(errorMessage = "No claim found, please try starting a new claim") }
    }
  }

  suspend fun openClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    when (val result = claimsRepository.openClaim()) {
      is ClaimResult.Error -> update { it.copy(errorMessage = result.message, isLoading = false) }
      is ClaimResult.Success -> update { it.copy(id = UUID.randomUUID(), isLoading = false) }
    }
  }

  suspend fun openClaimAndPayout(amount: MonetaryAmount) = with(_viewState) {
    update { it.copy(isLoadingPayment = true) }
    when (val result = claimsRepository.openClaim(amount)) {
      is ClaimResult.Error -> update { it.copy(errorMessage = result.message, isLoadingPayment = false) }
      is ClaimResult.Success -> update { it.copy(shouldExit = true, isLoadingPayment = false) }
    }
  }

  fun onNext() {
    _viewState.update { it.copy(id = UUID.randomUUID()) }
  }

  suspend fun onAudioFile(file: File) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      // upload file
      _viewState.update {
        val newState = currentClaim.state.copy(audioUrl = file.name)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onLocation(location: AutomationClaimDTO2.ClaimLocation) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      _viewState.update {
        val newState = currentClaim.state.copy(location = location)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onDateOfOccurrence(date: LocalDate) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      _viewState.update {
        val newState = currentClaim.state.copy(dateOfOccurrence = date)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onDateOfPurchase(date: LocalDate) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      _viewState.update {
        val item = currentClaim.state.item
        val newItem = item.copy(purchaseDate = date)
        val newState = currentClaim.state.copy(item = newItem)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onPurchasePrice(price: MonetaryAmount) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      _viewState.update {
        val item = currentClaim.state.item
        val newItem = item.copy(purchasePrice = price)
        val newState = currentClaim.state.copy(item = newItem)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onTypeOfDamage(problem: AutomationClaimInputDTO2.SingleItem.ClaimProblem) {
    val currentClaim = _viewState.value.claim
    if (currentClaim != null) {
      _viewState.update {
        val item = currentClaim.state.item
        val newItem = item.copy(problemIds = listOf(problem))
        val newState = currentClaim.state.copy(item = newItem)
        it.copy(claim = currentClaim.copy(state = newState))
      }
    }
  }

  fun onExit() {
    _viewState.update { it.copy(shouldExit = true) }
  }

  fun onLastScreen() {
    _viewState.update { it.copy(isLastScreen = true) }
  }

  fun onDismissError() {
    _viewState.update { it.copy(errorMessage = null) }
  }
}
