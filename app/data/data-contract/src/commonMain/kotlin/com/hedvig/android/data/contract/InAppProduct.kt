package com.hedvig.android.data.contract

import kotlinx.serialization.Serializable

/**
 * A product that can be bought natively in the app.
 */
@Serializable
enum class InAppProduct {
  ACCIDENT,
  PAYMENT_PROTECTION,
}
