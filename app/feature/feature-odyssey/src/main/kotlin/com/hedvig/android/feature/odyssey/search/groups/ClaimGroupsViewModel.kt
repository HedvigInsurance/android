package com.hedvig.android.feature.odyssey.search.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimGroupsViewModel(
  private val getClaimEntryPoints: GetNetworkClaimEntryPointGroupsUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimGroupsUiState())
  val uiState = _uiState

  init {
    loadClaimGroups()
  }

  fun loadClaimGroups() {
    _uiState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke().fold(
        ifLeft = { errorMessage ->
          _uiState.update {
            it.copy(
              errorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { result ->
          _uiState.update {
            it.copy(
              claimGroups = result.claimGroups,
              memberName = result.memberName,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaimGroup(claimGroup: ClaimGroup) {
    _uiState.update { it.copy(selectedClaim = claimGroup) }
  }

  fun resetState() {
    _uiState.update { it.copy(selectedClaim = null) }
  }
}
