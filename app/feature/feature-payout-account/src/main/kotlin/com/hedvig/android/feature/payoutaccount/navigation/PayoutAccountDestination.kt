package com.hedvig.android.feature.payoutaccount.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

sealed interface PayoutAccountDestination {
  @Serializable
  data object Graph : PayoutAccountDestination, HedvigNavKey
}

internal sealed interface PayoutAccountDestinations {
  @Serializable
  data class SelectPayoutMethod(
    val availableProviders: List<String>,
  ) : PayoutAccountDestinations, HedvigNavKey

  @Serializable
  data object EditBankAccount : PayoutAccountDestinations, HedvigNavKey

  @Serializable
  data object SetupSwishPayout : PayoutAccountDestinations, HedvigNavKey

  @Serializable
  data object SetupInvoicePayout : PayoutAccountDestinations, HedvigNavKey
}
