package com.hedvig.android.feature.inapppurchase.ui.summary

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.ui.InAppPurchasePlaceholderScreen

@Composable
internal fun InAppPurchaseSummaryDestination(
  productDisplayName: String,
  offer: InAppPurchaseOffer,
  navigateUp: () -> Unit,
  onSigned: (InAppPurchaseOffer) -> Unit,
) {
  InAppPurchaseSummaryScreen(
    productDisplayName = productDisplayName,
    offer = offer,
    navigateUp = navigateUp,
    onSign = dropUnlessResumed { onSigned(offer) },
  )
}

@Composable
private fun InAppPurchaseSummaryScreen(
  productDisplayName: String,
  offer: InAppPurchaseOffer,
  navigateUp: () -> Unit,
  onSign: () -> Unit,
) {
  InAppPurchasePlaceholderScreen(
    title = productDisplayName,
    description = "Step 3: confirm ${offer.exposureDisplayName}, ${offer.costBreakdown.size} breakdown lines, " +
      "${offer.cost.monthlyNet}/mo",
    navigateUp = navigateUp,
    actions = listOf("Sign with BankID" to onSign),
  )
}

@HedvigPreview
@Composable
private fun PreviewInAppPurchaseSummaryScreen() {
  HedvigTheme {
    Surface {
      InAppPurchaseSummaryScreen("Olycksfallsförsäkring", InAppPurchaseMocks.accidentQuote().offers.first(), {}, {})
    }
  }
}
