package com.hedvig.android.odyssey.resolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.repository.ClaimResult
import com.hedvig.android.odyssey.repository.ClaimsFlowRepository
import com.hedvig.odyssey.remote.money.MonetaryAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResolutionViewModel(
  val repository: ClaimsFlowRepository,
  val resolution: Resolution,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ResolutionViewState())
  val viewState = _viewState

  init {
    if (resolution == Resolution.ManualHandling) {
      viewModelScope.launch {
        openClaim()
      }
    }
  }

  private suspend fun openClaim() = with(_viewState) {
    update { it.copy(isLoading = true) }
    when (val result = repository.openClaim()) {
      is ClaimResult.Error -> _viewState.update { it.copy(errorMessage = result.message, isLoading = false) }
      is ClaimResult.Success -> update { it.copy(isLoading = false) }
    }
  }

  fun payout(amount: MonetaryAmount) {
    viewModelScope.launch {
      _viewState.update { it.copy(isLoadingPayout = true) }
      when (val result = repository.openClaim(amount)) {
        is ClaimResult.Error -> _viewState.update { it.copy(errorMessage = result.message, isLoadingPayout = false) }
        is ClaimResult.Success -> _viewState.update { it.copy(isLoadingPayout = false, isCompleted = true) }
      }
    }
  }

  fun onDismissError() {
    _viewState.update { it.copy(errorMessage = null, isLoading = false) }
  }
}
