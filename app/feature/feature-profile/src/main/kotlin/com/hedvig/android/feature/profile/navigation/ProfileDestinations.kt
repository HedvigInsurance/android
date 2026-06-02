package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

sealed interface ProfileDestination {
  @Serializable
  data object Graph : ProfileDestination, HedvigNavKey

  @Serializable
  data object Profile : ProfileDestination, HedvigNavKey

  @Serializable
  data object ContactInfo : ProfileDestination, HedvigNavKey
}

internal sealed interface ProfileDestinations {
  @Serializable
  data object Eurobonus : ProfileDestinations, HedvigNavKey

  @Serializable
  data object Certificates : ProfileDestinations, HedvigNavKey

  @Serializable
  data object Information : ProfileDestinations, HedvigNavKey

  @Serializable
  data object Licenses : ProfileDestinations, HedvigNavKey

  @Serializable
  data object SettingsGraph : ProfileDestinations, HedvigNavKey
}

internal sealed interface SettingsDestinations {
  @Serializable
  data object Settings : SettingsDestinations, HedvigNavKey
}

val profileBottomNavPermittedDestinations: List<KClass<out HedvigNavKey>> = listOf(
  ProfileDestinations.Eurobonus::class,
)

/*
* Not saving navigation state when explicitly logging out from Profile
*/
val destinationToExcludeFromSavingState: KClass<out HedvigNavKey> = ProfileDestination.Profile::class
