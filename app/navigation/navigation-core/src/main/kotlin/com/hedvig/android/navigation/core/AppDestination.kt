package com.hedvig.android.navigation.core

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface AppDestination : Destination {
  @Serializable
  data object Login : AppDestination

  @Serializable
  data class Chat(
    val chatContext: ChatContext? = null,
  ) : AppDestination {
    enum class ChatContext {
      PAYMENT,
      CLAIMS,
      COVERAGE,
      INSURANCE,
      OTHER,
    }
  }

  @Serializable
  data object ChangeAddress : AppDestination

  @Serializable
  data object EditCoInsured : AppDestination

  @Serializable
  data object TravelCertificate : AppDestination

  @Serializable
  data object ClaimsFlow : AppDestination

  @Serializable
  data class TerminationFlow(
    @SerialName("insuranceId")
    val insuranceId: String?,
  ) : AppDestination

  // Handles connecting payment with Trustly. Auto-navigates to Adyen for NO/DK
  @Serializable
  data object ConnectPayment : AppDestination

  // To be deprecated as soon as Adyen support is dropped
  @Serializable
  data object ConnectPaymentAdyen : AppDestination

  @Serializable
  data class ClaimDetails(
    val claimId: String,
  ) : AppDestination

  @Serializable
  data class CoInsuredAddInfo(
    val contractId: String,
  ) : AppDestination

  @Serializable
  data class CoInsuredAddOrRemove(
    val contractId: String,
  ) : AppDestination
}
