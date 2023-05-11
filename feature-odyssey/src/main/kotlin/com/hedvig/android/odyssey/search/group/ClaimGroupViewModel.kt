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
  private val _viewState = MutableStateFlow(ClaimGroupViewState())
  val viewState = _viewState

  init {
    loadClaimGroup()
  }

  fun loadClaimGroup() {
    _viewState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke(groupId).fold(
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
              searchableClaims = result.searchableClaims,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectSearchableClaim(searchableClaim: SearchableClaim) {
    _viewState.update { it.copy(selectedClaim = searchableClaim) }
  }

  fun resetState() {
    _viewState.update { it.copy(selectedClaim = null) }
  }
}
