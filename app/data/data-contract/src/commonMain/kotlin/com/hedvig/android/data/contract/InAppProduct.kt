package com.hedvig.android.data.contract

import kotlinx.serialization.Serializable

/**
 * A product that can be bought natively in the app.
 */
@Serializable
enum class InAppProduct {
  ACCIDENT,
  PAYMENT_PROTECTION,
  DOG,
  CAT,
  CAR,
  APARTMENT_RENT,
  APARTMENT_BRF,
  APARTMENT_STUDENT,
  HOUSE,
  VACATION_HOME,
}
