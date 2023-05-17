package com.hedvig.android.feature.odyssey.search.commonclaims

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.odyssey.search.group.GetClaimEntryGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SearchViewModel(
  private val getClaimEntryPoints: GetClaimEntryGroupUseCase,
) : ViewModel() {
  private val _viewState = MutableStateFlow(SearchViewState())
  val viewState = _viewState

  init {
    loadSearchableClaims()
  }

  fun loadSearchableClaims() {
    _viewState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke(null).fold(
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
              commonClaims = result.searchableClaims,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaim(searchableClaim: SearchableClaim) {
    _viewState.update { it.copy(selectedClaim = searchableClaim) }
  }

  fun resetState() {
    _viewState.update { it.copy(selectedClaim = null) }
  }
}
