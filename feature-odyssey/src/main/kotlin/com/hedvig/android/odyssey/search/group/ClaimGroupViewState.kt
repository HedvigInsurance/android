package com.hedvig.android.odyssey.search.group

import com.hedvig.android.odyssey.model.SearchableClaim

internal data class ClaimGroupViewState(
  val input: String? = null,
  val searchableClaims: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
