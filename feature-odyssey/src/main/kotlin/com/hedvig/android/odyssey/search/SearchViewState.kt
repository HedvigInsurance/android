package com.hedvig.android.odyssey.search

import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim

data class SearchViewState(
  val input: String? = null,
  val commonClaims: List<SearchableClaim> = listOf(),
  val results: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)

