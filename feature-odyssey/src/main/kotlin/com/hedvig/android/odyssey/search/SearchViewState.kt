package com.hedvig.android.odyssey.search

import com.hedvig.android.odyssey.model.ItemProblem
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim

data class SearchViewState(
  val input: String? = null,
  val commonClaims: List<SearchableClaim> = listOf(
    SearchableClaim(
      id = "1",
      displayName = "Broken phone",
      itemType = ItemType("PHONE"),
      itemProblem = ItemProblem("BROKEN"),
    ),
    SearchableClaim(
      id = "2",
      displayName = "Broken computer",
      itemType = ItemType("COMPUTER"),
      itemProblem = ItemProblem("BROKEN"),
    ),
    SearchableClaim(
      id = "3",
      displayName = "Stolen bike",
      itemType = ItemType("BIKE"),
      itemProblem = ItemProblem("STOLEN"),
    ),
    SearchableClaim(
      id = "4",
      displayName = "Other",
      itemType = ItemType(""),
    ),
  ),
  val results: List<SearchableClaim> = emptyList(),
  val selectedClaim: SearchableClaim? = null,
  val showCommonClaims: Boolean = true,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)

