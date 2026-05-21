package com.hedvig.android.feature.purchase.car.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class CarPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface CarPurchaseDestination {
  @Serializable
  data object Form : CarPurchaseDestination, Destination
}
