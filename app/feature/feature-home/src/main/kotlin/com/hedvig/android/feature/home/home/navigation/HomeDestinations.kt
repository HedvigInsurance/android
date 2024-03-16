package com.hedvig.android.feature.home.home.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface HomeDestination : Destination {
  @Serializable
  data object Graph : HomeDestination

  @Serializable
  data object Home : HomeDestination
}
