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

  @Serializable
  object MyInfo : AppDestination

  @Serializable
  object AboutApp : AppDestination

  @Serializable
  object Licenses : AppDestination

  @Serializable
  data class ContractDetail(val contractId: String) : AppDestination

  @Serializable
  object Settings : AppDestination

  @Serializable
  object PaymentInfo : AppDestination
//  @Serializable
//  object LegacyClaimsTriaging : AppDestination
}
