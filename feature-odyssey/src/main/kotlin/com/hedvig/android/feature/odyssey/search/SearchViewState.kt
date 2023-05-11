package com.hedvig.android.feature.odyssey.search

import com.hedvig.android.feature.odyssey.model.SearchableClaim

internal data class SearchViewState(
  val input: String? = null,
  val commonClaims: List<SearchableClaim> = listOf(),
  val results: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
