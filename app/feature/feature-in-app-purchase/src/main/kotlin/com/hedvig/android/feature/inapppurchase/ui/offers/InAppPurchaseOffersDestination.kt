package com.hedvig.android.feature.inapppurchase.ui.offers

import androidx.compose.runtime.Composable
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.ui.InAppPurchasePlaceholderScreen

@Composable
internal fun InAppPurchaseOffersDestination(
  productDisplayName: String,
  offers: List<InAppPurchaseOffer>,
  navigateUp: () -> Unit,
  onOfferSelected: (InAppPurchaseOffer) -> Unit,
) {
  InAppPurchaseOffersScreen(
    productDisplayName = productDisplayName,
    offers = offers,
    navigateUp = navigateUp,
    onOfferSelected = dropUnlessResumed { offer: InAppPurchaseOffer -> onOfferSelected(offer) },
  )
}

@Composable
private fun InAppPurchaseOffersScreen(
  productDisplayName: String,
  offers: List<InAppPurchaseOffer>,
  navigateUp: () -> Unit,
  onOfferSelected: (InAppPurchaseOffer) -> Unit,
) {
  InAppPurchasePlaceholderScreen(
    title = productDisplayName,
    description = "Step 2: choose one of ${offers.size} offers",
    navigateUp = navigateUp,
    actions = offers.map { offer ->
      "${offer.exposureDisplayName} · ${offer.cost.monthlyNet}" to { onOfferSelected(offer) }
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewInAppPurchaseOffersScreen() {
  HedvigTheme {
    Surface {
      InAppPurchaseOffersScreen("Betalskydd", InAppPurchaseMocks.paymentProtectionQuote().offers, {}, {})
    }
  }
}
