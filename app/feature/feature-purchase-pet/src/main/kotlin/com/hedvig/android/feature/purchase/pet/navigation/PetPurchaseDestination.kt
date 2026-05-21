package com.hedvig.android.feature.purchase.pet.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data object PetPurchaseGraphDestination : Destination

internal sealed interface PetPurchaseDestination {
  @Serializable
  data object SpeciesPicker : PetPurchaseDestination, Destination

  @Serializable
  data class Form(val productName: String) : PetPurchaseDestination, Destination
}
