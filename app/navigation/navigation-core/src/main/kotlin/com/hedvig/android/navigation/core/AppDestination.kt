package com.hedvig.android.navigation.core

import com.hedvig.android.data.contract.ContractGroup
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface AppDestination : Destination {
  sealed interface TopLevelDestination : AppDestination {
    @Serializable
    data object Home : TopLevelDestination

    @Serializable
    data object Insurance : TopLevelDestination

    @Serializable
    data object Profile : TopLevelDestination

    @Serializable
    data object Forever : TopLevelDestination
  }

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
  data object Eurobonus : AppDestination

  @Serializable
  data object ClaimsFlow : AppDestination

  @Serializable
  data object MyInfo : AppDestination

  @Serializable
  data object AboutApp : AppDestination

  @Serializable
  data object Licenses : AppDestination

  @Serializable
  data object Settings : AppDestination

  @Serializable
  data class TerminateInsurance(
    val contractId: String,
    val insuranceDisplayName: String,
    val exposureName: String,
    val contractGroup: ContractGroup,
  ) : AppDestination

  @Serializable
  data object PaymentInfo : AppDestination

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
