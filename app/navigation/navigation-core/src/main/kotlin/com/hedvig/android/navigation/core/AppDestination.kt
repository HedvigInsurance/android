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
    object Forever : TopLevelDestination
  }

  @Serializable
  object Login : AppDestination

  @Serializable
  object ChangeAddress : AppDestination

  @Serializable
  object GenerateTravelCertificate : AppDestination

  @Serializable
  object Eurobonus : AppDestination

  @Serializable
  object ClaimsFlow : AppDestination

  @Serializable
  object MyInfo : AppDestination

  @Serializable
  object AboutApp : AppDestination

  @Serializable
  object Licenses : AppDestination

  @Serializable
  object Settings : AppDestination

  @Serializable
  data class TerminateInsurance(
    val insuranceId: String,
    val insuranceDisplayName: String,
  ) : AppDestination

  @Serializable
  object PaymentInfo : AppDestination

  @Serializable
  object PaymentHistory : AppDestination
//  @Serializable
//  object LegacyClaimsTriaging : AppDestination
}
