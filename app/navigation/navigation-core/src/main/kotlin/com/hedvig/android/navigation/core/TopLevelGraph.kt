package com.hedvig.android.navigation.core

sealed interface TopLevelGraph {
  data object Home : TopLevelGraph

  data object Insurances : TopLevelGraph

  data object Payments : TopLevelGraph

  data object Profile : TopLevelGraph
}
