package com.hedvig.android.feature.purchase.pet.data

import com.hedvig.android.core.uidata.UiMoney

internal const val PRODUCT_NAME_DOG = "SE_PET_DOG"
internal const val PRODUCT_NAME_CAT = "SE_PET_CAT"

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
  val ssn: String,
  val email: String,
)

internal data class Breed(
  val id: String,
  val displayName: String,
  val isMixedBreed: Boolean,
)

internal data class PetOffers(
  val productDisplayName: String,
  val offers: List<PetTierOffer>,
)

internal data class PetTierOffer(
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

internal enum class PetGender { MALE, FEMALE }
