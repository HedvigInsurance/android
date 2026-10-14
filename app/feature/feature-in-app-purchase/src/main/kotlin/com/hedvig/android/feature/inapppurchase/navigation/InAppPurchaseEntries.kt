package com.hedvig.android.feature.inapppurchase.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseQuote
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormDestination
import com.hedvig.android.feature.inapppurchase.ui.offers.InAppPurchaseOffersDestination
import com.hedvig.android.feature.inapppurchase.ui.productinfo.InAppPurchaseProductInfoDestination
import com.hedvig.android.feature.inapppurchase.ui.success.InAppPurchaseSuccessScreen
import com.hedvig.android.feature.inapppurchase.ui.summary.InAppPurchaseSummaryDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo

fun EntryProviderScope<HedvigNavKey>.inAppPurchaseEntries(backstack: Backstack) {
  entry<InAppPurchaseKey> { key ->
    InAppPurchaseFormDestination(
      product = key.product,
      navigateUp = backstack::navigateUp,
      onLearnMore = { product -> backstack.add(InAppPurchaseProductInfoKey(product)) },
      onQuoted = { productDisplayName, quote ->
        backstack.navigateToOffers(productDisplayName, quote)
      },
    )
  }

  entry<InAppPurchaseProductInfoKey> { key ->
    InAppPurchaseProductInfoDestination(
      product = key.product,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<InAppPurchaseOffersKey> { key ->
    InAppPurchaseOffersDestination(
      productDisplayName = key.productDisplayName,
      offers = key.offers,
      navigateUp = backstack::navigateUp,
      onOfferSelected = { offer ->
        backstack.add(InAppPurchaseSummaryKey(key.productDisplayName, key.shopSessionId, offer))
      },
    )
  }

  entry<InAppPurchaseSummaryKey> { key ->
    InAppPurchaseSummaryDestination(
      productDisplayName = key.productDisplayName,
      offer = key.offer,
      navigateUp = backstack::navigateUp,
      onSigned = { offer: InAppPurchaseOffer ->
        backstack.navigateAndPopUpTo<InAppPurchaseKey>(InAppPurchaseSuccessKey(offer.startDate), inclusive = true)
      },
    )
  }

  entry<InAppPurchaseSuccessKey> { key ->
    InAppPurchaseSuccessScreen(
      startDate = key.startDate,
      close = backstack::popBackstack,
    )
  }
}

/**
 * With a single offer there is nothing to choose, so the offers step is skipped.
 */
private fun Backstack.navigateToOffers(productDisplayName: String, quote: InAppPurchaseQuote.Quoted) {
  val singleOffer = quote.offers.singleOrNull()
  if (singleOffer != null) {
    add(InAppPurchaseSummaryKey(productDisplayName, quote.shopSessionId, singleOffer))
  } else {
    add(InAppPurchaseOffersKey(productDisplayName, quote.shopSessionId, quote.offers))
  }
}
