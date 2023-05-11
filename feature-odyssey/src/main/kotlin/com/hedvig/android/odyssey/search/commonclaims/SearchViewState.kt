package com.hedvig.android.odyssey.search.commonclaims

internal data class SearchViewState(
  val input: String? = null,
  val commonClaims: List<SearchableClaim> = listOf(),
  val results: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
