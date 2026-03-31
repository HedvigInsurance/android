package com.hedvig.android.feature.purchase.apartment.data

import com.hedvig.android.core.uidata.UiMoney

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

internal data class ApartmentOffers(
  val productDisplayName: String,
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

internal data class SigningStart(
  val signingId: String,
  val autoStartToken: String,
)

internal data class SigningPollResult(
  val status: SigningStatus,
  val liveQrCodeData: String?,
)

internal enum class SigningStatus {
  PENDING,
  SIGNED,
  FAILED,
}
