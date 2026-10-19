package com.hedvig.android.feature.inapppurchase.navigation

import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseQuote
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add

/**
 * With a single offer there is nothing to choose, so the offers step is skipped.
 */
internal fun Backstack.navigateToOffers(productDisplayName: String, quote: InAppPurchaseQuote.Quoted) {
  val singleOffer = quote.offers.singleOrNull()
  if (singleOffer != null) {
    add(InAppPurchaseSummaryKey(productDisplayName, quote.shopSessionId, singleOffer))
  } else {
    add(InAppPurchaseOffersKey(productDisplayName, quote.shopSessionId, quote.offers))
  }
}
