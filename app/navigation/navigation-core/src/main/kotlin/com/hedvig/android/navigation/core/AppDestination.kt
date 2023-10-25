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
  object Chat : AppDestination

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

  @Serializable
  object ConnectPayment : AppDestination // Handles connecting payment with Trustly. Auto-navigates to Adyen for NO/DK

  @Serializable
  object ConnectPaymentAdyen : AppDestination // To be deprecated as soon as Adyen support is dropped
}
