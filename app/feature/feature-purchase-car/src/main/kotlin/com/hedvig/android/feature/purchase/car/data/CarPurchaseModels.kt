package com.hedvig.android.feature.purchase.car.data

import com.hedvig.android.core.uidata.UiMoney

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

internal data class CarOffers(
  val productDisplayName: String,
  val offers: List<CarTierOffer>,
)

internal data class CarTierOffer(
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
