package com.hedvig.android.feature.payoutaccount.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

sealed interface PayoutAccountDestination {
  @Serializable
  data object Graph : PayoutAccountDestination, Destination
}

internal sealed interface PayoutAccountDestinations {
  @Serializable
  data object Overview : PayoutAccountDestinations, Destination

  @Serializable
  data class SelectPayoutMethod(
    val availableProviders: List<String>,
  ) : PayoutAccountDestinations, Destination

  @Serializable
  data object EditBankAccount : PayoutAccountDestinations, Destination

  @Serializable
  data object SetupSwishPayout : PayoutAccountDestinations, Destination
}
