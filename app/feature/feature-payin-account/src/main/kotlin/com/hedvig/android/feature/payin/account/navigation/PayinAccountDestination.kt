package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

sealed interface PayinAccountDestination {
  @Serializable
  data object Graph : PayinAccountDestination, Destination
}

internal sealed interface PayinAccountDestinations {
  @Serializable
  data object Overview : PayinAccountDestinations, Destination

  @Serializable
  data class SelectPayinMethod(
    val availableProviders: List<String>,
  ) : PayinAccountDestinations, Destination

  @Serializable
  data object SetupSwishPayin : PayinAccountDestinations, Destination

  @Serializable
  data object SetupInvoicePayin : PayinAccountDestinations, Destination
}
