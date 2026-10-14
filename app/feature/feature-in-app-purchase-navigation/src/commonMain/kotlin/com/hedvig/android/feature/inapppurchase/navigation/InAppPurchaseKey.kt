package com.hedvig.android.feature.inapppurchase.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
enum class InAppProduct {
  ACCIDENT,
  PAYMENT_PROTECTION,
}

/**
 * The start of the in-app purchase flow for [product].
 */
@Serializable
data class InAppPurchaseKey(val product: InAppProduct) : HedvigNavKey

/**
 * The product a cross-sell sells, if it can be bought in the app.
 *
 * TODO: `CrossSell` carries no product type, so this matches the last path segment of its `storeUrl`, in English or
 *  Swedish (for example `https://www.hedvig.com/se/forsakringar/olycksfallsforsakring`). Replace with a product field
 *  on `CrossSell`.
 */
fun inAppProductForCrossSellStoreUrl(storeUrl: String): InAppProduct? {
  return when (storeUrl.substringBefore('?').trimEnd('/').substringAfterLast('/')) {
    "accident", "olycksfallsforsakring" -> InAppProduct.ACCIDENT
    "payment-protection", "trygghetsforsakring" -> InAppProduct.PAYMENT_PROTECTION
    else -> null
  }
}
