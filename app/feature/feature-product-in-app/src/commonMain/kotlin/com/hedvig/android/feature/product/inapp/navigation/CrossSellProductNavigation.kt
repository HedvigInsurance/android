package com.hedvig.android.feature.product.inapp.navigation

import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo

/**
 * Opens the product page of [crossSell], or, for a generic cross-sell, the sheet to pick one of its options from.
 */
fun Backstack.navigateToCrossSellProduct(crossSell: CrossSell) {
  if (crossSell.options.isEmpty()) {
    add(crossSell.toCrossSellProductKey())
  } else {
    add(CrossSellOptionsKey(options = crossSell.options.map { it.toCrossSellProductKey() }))
  }
}

/**
 * Shows [result] in place of the product page and the purchase flow above it, so leaving it returns to wherever the
 * cross-sell was tapped.
 */
fun Backstack.showCrossSellPurchaseResult(result: HedvigNavKey) {
  navigateAndPopUpTo<CrossSellProductKey>(result, inclusive = true)
}

/**
 * Replaces the options sheet with the chosen product page, so leaving that page returns to wherever the
 * cross-sell was tapped rather than to the sheet.
 */
internal fun Backstack.navigateToSelectedOption(option: CrossSellProductKey) {
  navigateAndPopUpTo<CrossSellOptionsKey>(option, inclusive = true)
}

private fun CrossSell.toCrossSellProductKey(): CrossSellProductKey = CrossSellProductKey(
  crossSellId = id,
  title = title,
  subtitle = subtitle,
  pillowImageSrc = pillowImageSmall.src,
  storeUrl = storeUrl,
  discountText = null,
)
