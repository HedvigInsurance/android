package com.hedvig.android.feature.profile.navigation

import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.serialization.Serializable

sealed interface ProfileDestination : Destination {
  @Serializable
  data object Graph : ProfileDestination

  @Serializable
  data object Profile : ProfileDestination
}

internal sealed interface ProfileDestinations : Destination {
  @Serializable
  data object Eurobonus : ProfileDestinations

  @Serializable
  data object MyInfo : ProfileDestinations

  @Serializable
  data object AboutApp : ProfileDestinations

  @Serializable
  data object Licenses : ProfileDestinations

  @Serializable
  data object SettingsGraph : ProfileDestinations
}

internal sealed interface SettingsDestinations {
  @Serializable
  data object Settings : ProfileDestinations
}

val profileBottomNavPermittedDestinations: List<String> = listOf(
  createRoutePattern<ProfileDestinations.Eurobonus>(),
)
