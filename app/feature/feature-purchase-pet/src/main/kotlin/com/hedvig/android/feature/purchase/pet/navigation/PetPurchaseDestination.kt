package com.hedvig.android.feature.purchase.pet.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class PetPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface PetPurchaseDestination {
  @Serializable
  data object Form : PetPurchaseDestination, Destination
}
