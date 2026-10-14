package com.hedvig.android.feature.inapppurchase.ui.productinfo

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.feature.inapppurchase.ui.InAppPurchasePlaceholderScreen

@Composable
internal fun InAppPurchaseProductInfoDestination(product: InAppPurchaseProduct, navigateUp: () -> Unit) {
  InAppPurchaseProductInfoScreen(product = product, navigateUp = navigateUp)
}

@Composable
private fun InAppPurchaseProductInfoScreen(product: InAppPurchaseProduct, navigateUp: () -> Unit) {
  val variant = product.variants.firstOrNull()
  InAppPurchasePlaceholderScreen(
    title = product.displayNameFull,
    description = "Learn more: ${product.tagline}. ${variant?.perils?.size ?: 0} perils, " +
      "${variant?.insurableLimits?.size ?: 0} limits, ${variant?.documents?.size ?: 0} documents, " +
      "${product.faq.size} FAQ items",
    navigateUp = navigateUp,
    actions = emptyList(),
  )
}

@HedvigPreview
@Composable
private fun PreviewInAppPurchaseProductInfoScreen() {
  HedvigTheme {
    Surface {
      InAppPurchaseProductInfoScreen(InAppPurchaseMocks.accidentForm.product, {})
    }
  }
}
