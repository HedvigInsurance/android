package com.hedvig.android.odyssey.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
  private val getCommonClaimsUseCase: GetCommonClaimsUseCase,
) : ViewModel() {
  private val searchableClaims = listOf(
    SearchableClaim(
      id = "1",
      displayName = "Broken phone",
      itemType = ItemType("PHONE"),
      hasQuickPayout = true,
      keywords = listOf("mobile", "ios", "android"),
    ),
    SearchableClaim(
      id = "2",
      displayName = "Broken computer",
      itemType = ItemType("COMPUTER"),
      hasQuickPayout = true,
      keywords = listOf("laptop", "broken"),
    ),
    SearchableClaim(
      id = "3",
      displayName = "Stolen bike",
      itemType = ItemType("BIKE"),
      hasQuickPayout = true,
      keywords = listOf("laptop"),
    ),
    SearchableClaim(
      id = "4",
      displayName = "Delayed Luggage",
      itemType = ItemType(""),
      keywords = listOf("travel"),
    ),
    SearchableClaim(
      id = "5",
      displayName = "Accident Abroad",
      itemType = ItemType(""),
    ),
    SearchableClaim(
      id = "6",
      displayName = "Water damage",
      itemType = ItemType(""),
    ),
    SearchableClaim(
      id = "7",
      displayName = "Fire",
      itemType = ItemType(""),
    ),
    SearchableClaim(
      id = "8",
      displayName = "Car damage",
      itemType = ItemType(""),
      isCovered = false,
    ),
    SearchableClaim(
      id = "9",
      displayName = "Personal damage",
      itemType = ItemType(""),
    ),
    SearchableClaim(
      id = "10",
      displayName = "Veterinary visit",
      itemType = ItemType(""),
      isCovered = false,
    ),
  )
  private val _viewState = MutableStateFlow(SearchViewState(results = searchableClaims))
  val viewState = _viewState

  init {
    viewModelScope.launch {
      when (val result = getCommonClaimsUseCase.invoke()) {
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
}
