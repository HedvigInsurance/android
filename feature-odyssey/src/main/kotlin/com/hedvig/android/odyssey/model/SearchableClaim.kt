package com.hedvig.android.odyssey.model

data class SearchableClaim(
  val entryPointId: String,
  val displayName: String,
  val itemType: ItemType? = null,
  val itemProblem: ItemProblem? = null,
  val icon: Icon? = null,
  val keywords: List<String> = emptyList(),
  val hasQuickPayout: Boolean = false,
  val isCovered: Boolean = true,
) {
  data class Icon(
    val darkUrl: String,
    val lightUrl: String,
  )
}

fun SearchableClaim.icon(isInDarkTheme: Boolean): String? {
  return if (isInDarkTheme) {
    icon?.darkUrl
  } else {
    icon?.lightUrl
  }
}
