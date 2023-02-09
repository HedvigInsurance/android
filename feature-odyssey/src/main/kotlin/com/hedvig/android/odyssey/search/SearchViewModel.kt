package com.hedvig.android.odyssey.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
  private val getClaimEntryPoints: GetClaimEntryPoints,
) : ViewModel() {
  private val searchableClaims = emptyList<SearchableClaim>()
  private val _viewState = MutableStateFlow(SearchViewState(results = searchableClaims))
  val viewState = _viewState

  init {
    viewModelScope.launch {
      when (val result = getClaimEntryPoints.invoke()) {
        is CommonClaimsResult.Error -> _viewState.update { it.copy(errorMessage = result.message, isLoading = false) }
        is CommonClaimsResult.Success -> _viewState.update {
          it.copy(
            commonClaims = result.searchableClaims,
            isLoading = false,
          )
        }
      }
    }
  }

  fun onInput(input: String) {
    val results = searchableClaims.filter {
      it.displayName.contains(input, ignoreCase = true)
        || it.keywords.any { it.contains(input) }
    }
    _viewState.update { it.copy(input = input, results = results, selectedClaim = null) }
  }

  fun onSelectClaim(searchableClaim: SearchableClaim) {
    _viewState.update { it.copy(selectedClaim = searchableClaim) }
  }

  fun onShowCommonClaims(show: Boolean) {
    _viewState.update { it.copy(showCommonClaims = show, selectedClaim = null) }
  }

  fun onCantFind() {
    _viewState.update {
      it.copy(
        selectedClaim = SearchableClaim(
          id = "",
          displayName = "Unknown",
          itemType = ItemType(""),
        ),
      )
    }
  }

  fun resetState() {
    _viewState.update { it.copy(selectedClaim = null) }
  }
}
