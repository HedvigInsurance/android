package com.hedvig.android.feature.purchase.car.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
  val ssn: String,
  val email: String,
)

internal data class CarOffers(
  val productDisplayName: String,
  val contractGroup: ContractGroup,
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
