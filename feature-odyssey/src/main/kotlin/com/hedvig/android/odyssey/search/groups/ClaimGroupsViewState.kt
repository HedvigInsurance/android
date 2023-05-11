package com.hedvig.android.odyssey.search.groups

internal data class ClaimGroupsViewState(
  val input: String? = null,
  val claimGroups: List<ClaimGroup> = listOf(),
  val results: List<ClaimGroup> = emptyList(),
  val selectedClaim: ClaimGroup? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
