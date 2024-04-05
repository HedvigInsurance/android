package com.hedvig.android.feature.home.home.navigation

import kotlinx.serialization.Serializable

sealed interface HomeDestination {
  @Serializable
  data object Graph : HomeDestination

  @Serializable
  data object Home : HomeDestination
}
