package com.hedvig.android.odyssey.search.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimGroupViewModel(
  private val getClaimEntryPoints: GetClaimEntryGroupUseCase,
  private val groupId: String,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimGroupUiState())
  val uiState = _uiState

  init {
    loadClaimGroup()
  }

  fun loadClaimGroup() {
    _uiState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke(groupId).fold(
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
              searchableClaims = result.searchableClaims,
              selectedClaim = result.searchableClaims.lastOrNull(),
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectSearchableClaim(searchableClaim: SearchableClaim) {
    _uiState.update { it.copy(selectedClaim = searchableClaim) }
  }

  fun resetState() {
    _uiState.update { it.copy(selectedClaim = null) }
  }
}
