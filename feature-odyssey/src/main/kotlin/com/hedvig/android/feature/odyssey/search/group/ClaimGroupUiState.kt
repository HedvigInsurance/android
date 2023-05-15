package com.hedvig.android.feature.odyssey.search.group

import com.hedvig.android.feature.odyssey.search.commonclaims.SearchableClaim

internal data class ClaimGroupUiState(
  val input: String? = null,
  val searchableClaims: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
