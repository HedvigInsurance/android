package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.Destination
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

sealed interface ProfileDestination {
  @Serializable
  data object Graph : ProfileDestination, Destination

  @Serializable
  data object Profile : ProfileDestination, Destination

  @Serializable
  data object ContactInfo : ProfileDestination, Destination
}

internal sealed interface ProfileDestinations {
  @Serializable
  data object Eurobonus : ProfileDestinations, Destination

  @Serializable
  data object Certificates : ProfileDestinations, Destination

  @Serializable
  data object AboutApp : ProfileDestinations, Destination

  @Serializable
  data object Licenses : ProfileDestinations, Destination

  @Serializable
  data object SettingsGraph : ProfileDestinations, Destination
}

internal sealed interface SettingsDestinations {
  @Serializable
  data object Settings : SettingsDestinations, Destination
}

val profileBottomNavPermittedDestinations: List<KClass<out Destination>> = listOf(
  ProfileDestinations.Eurobonus::class,
)
