package com.hedvig.android.navigation.core

import androidx.annotation.Keep
import com.hedvig.android.navigation.compose.Destination
import kotlinx.serialization.Serializable

sealed interface AppDestination {
  @Serializable
  data object ChangeAddress : AppDestination, Destination

  // Workaround for https://issuetracker.google.com/issues/353898971
  @Keep
  @Serializable
  data object EditCoInsured : AppDestination, Destination

  @Serializable
  data object TravelCertificate : AppDestination, Destination

  @Serializable
  data object ClaimsFlow : AppDestination, Destination

  // Handles connecting payment with Trustly. Auto-navigates to Adyen for NO/DK
  @Serializable
  data object ConnectPayment : AppDestination, Destination

  // To be deprecated as soon as Adyen support is dropped
  @Serializable
  data object ConnectPaymentAdyen : AppDestination, Destination

  @Serializable
  data class ClaimDetails(
    val claimId: String,
  ) : AppDestination, Destination

  @Serializable
  data class CoInsuredAddInfo(
    val contractId: String,
  ) : AppDestination, Destination

  @Serializable
  data class CoInsuredAddOrRemove(
    val contractId: String,
  ) : AppDestination, Destination
}
