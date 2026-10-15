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
 * TODO: `CrossSell` carries no product type, so this matches the last path segment of the product page, in English
 *  or Swedish (for example `/se-en/insurances/payment-protection`). Replace with a product field on `CrossSell`.
 */
fun inAppProductForCrossSellStoreUrl(storeUrl: String): InAppProduct? {
  // The backend hands out a redirect link whose `next` query parameter holds the product page path.
  val productPath = storeUrl.queryParameter("next")?.percentDecoded() ?: storeUrl.substringBefore('?')
  return when (productPath.substringBefore('?').trimEnd('/').substringAfterLast('/')) {
    "accident", "olycksfallsforsakring" -> InAppProduct.ACCIDENT
    "payment-protection", "trygghetsforsakring" -> InAppProduct.PAYMENT_PROTECTION
    else -> null
  }
}

private fun String.queryParameter(name: String): String? {
  return substringAfter('?', missingDelimiterValue = "")
    .split('&')
    .firstOrNull { it.substringBefore('=') == name }
    ?.substringAfter('=', missingDelimiterValue = "")
}

private fun String.percentDecoded(): String {
  val bytes = ArrayList<Byte>(length)
  var index = 0
  while (index < length) {
    val char = this[index]
    if (char == '%' && index + 2 < length) {
      val byte = substring(index + 1, index + 3).toIntOrNull(16)
      if (byte != null) {
        bytes.add(byte.toByte())
        index += 3
        continue
      }
    }
    bytes.addAll((if (char == '+') " " else char.toString()).encodeToByteArray().toList())
    index++
  }
  return bytes.toByteArray().decodeToString()
}
