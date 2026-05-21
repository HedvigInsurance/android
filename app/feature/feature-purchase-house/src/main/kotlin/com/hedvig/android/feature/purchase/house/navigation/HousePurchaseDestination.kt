package com.hedvig.android.feature.purchase.house.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class HousePurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface HousePurchaseDestination {
  @Serializable
  data object Form : HousePurchaseDestination, Destination
}
