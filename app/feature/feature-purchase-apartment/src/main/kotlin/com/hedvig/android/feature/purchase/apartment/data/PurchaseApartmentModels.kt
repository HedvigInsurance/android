package com.hedvig.android.feature.purchase.apartment.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

internal data class ApartmentOffers(
  val productDisplayName: String,
  val contractGroup: ContractGroup,
  val offers: List<ApartmentTierOffer>,
)

internal data class ApartmentTierOffer(
  val offerId: String,
  val tierDisplayName: String,
  val tierDescription: String,
  val grossPrice: UiMoney,
  val netPrice: UiMoney,
  val usps: List<String>,
  val exposureDisplayName: String,
  val deductibleDisplayName: String?,
  val hasDiscount: Boolean,
)
