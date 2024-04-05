package com.hedvig.android.feature.profile.navigation

import androidx.navigation.serialization.generateRouteWithArgs
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

sealed interface ProfileDestination {
  @Serializable
  data object Graph : ProfileDestination

  @Serializable
  data object Profile : ProfileDestination
}

internal sealed interface ProfileDestinations {
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

val profileBottomNavPermittedDestinations: List<KClass<*>> = listOf(
  ProfileDestinations.Eurobonus::class,
)
