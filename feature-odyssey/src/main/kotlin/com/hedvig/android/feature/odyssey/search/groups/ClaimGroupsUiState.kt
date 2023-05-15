package com.hedvig.android.feature.odyssey.search.groups

internal data class ClaimGroupsUiState(
  val input: String? = null,
  val claimGroups: List<ClaimGroup> = listOf(),
  val memberName: String? = null,
  val selectedClaim: ClaimGroup? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
