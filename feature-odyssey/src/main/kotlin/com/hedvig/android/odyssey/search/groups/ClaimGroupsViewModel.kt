package com.hedvig.android.odyssey.search.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimGroupsViewModel(
  private val getClaimEntryPoints: GetNetworkClaimEntryPointGroupsUseCase,
) : ViewModel() {
  private val _viewState = MutableStateFlow(ClaimGroupsViewState())
  val viewState = _viewState

  init {
    loadSearchableClaims()
  }

  fun loadSearchableClaims() {
    _viewState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke().fold(
        ifLeft = { errorMessage ->
          _viewState.update {
            it.copy(
              errorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { result ->
          _viewState.update {
            it.copy(
              claimGroups = result.claimGroups,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaimGroup(claimGroup: ClaimGroup) {
    _viewState.update { it.copy(selectedClaim = claimGroup) }
  }

  fun resetState() {
    _viewState.update { it.copy(selectedClaim = null) }
  }
}
