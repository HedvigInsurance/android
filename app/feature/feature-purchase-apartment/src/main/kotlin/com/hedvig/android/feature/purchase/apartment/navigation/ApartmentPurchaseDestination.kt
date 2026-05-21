package com.hedvig.android.feature.purchase.apartment.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class ApartmentPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface ApartmentPurchaseDestination {
  @Serializable
  data object Form : ApartmentPurchaseDestination, Destination
}
