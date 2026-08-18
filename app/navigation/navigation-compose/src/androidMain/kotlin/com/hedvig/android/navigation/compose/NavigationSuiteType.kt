package com.hedvig.android.navigation.compose

@JvmInline
value class NavigationSuiteType private constructor(
  private val description: String,
) {
  override fun toString(): String = description

  companion object {
    val NavigationBar = NavigationSuiteType(description = "NavigationBar")
    val NavigationRail = NavigationSuiteType(description = "NavigationRail")
    val NavigationRailXLarge = NavigationSuiteType(description = "NavigationRailXL")
  }
}
