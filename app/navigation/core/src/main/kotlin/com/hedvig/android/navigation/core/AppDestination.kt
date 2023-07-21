package com.hedvig.android.navigation.core

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface AppDestination : Destination {
  sealed interface TopLevelDestination : AppDestination {
    @Serializable
    object Home : TopLevelDestination

    @Serializable
    object Insurance : TopLevelDestination

    @Serializable
    object Profile : TopLevelDestination

    @Serializable
    object Referrals : TopLevelDestination
  }

  @Serializable
  object ChangeAddress : AppDestination

  @Serializable
  object GenerateTravelCertificate : AppDestination

  @Serializable
  object Eurobonus : AppDestination

  @Serializable
  object BusinessModel : AppDestination

  @Serializable
  object ClaimsFlow : AppDestination

//  @Serializable
//  object LegacyClaimsTriaging : AppDestination
}
