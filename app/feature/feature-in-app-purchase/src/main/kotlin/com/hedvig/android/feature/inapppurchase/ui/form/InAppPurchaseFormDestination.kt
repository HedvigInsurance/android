package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseQuote
import com.hedvig.android.feature.inapppurchase.navigation.InAppProduct
import com.hedvig.android.feature.inapppurchase.ui.InAppPurchasePlaceholderScreen

@Composable
internal fun InAppPurchaseFormDestination(
  product: InAppProduct,
  navigateUp: () -> Unit,
  onLearnMore: (InAppPurchaseProduct) -> Unit,
  onQuoted: (productDisplayName: String, quote: InAppPurchaseQuote.Quoted) -> Unit,
) {
  val (form, quote) = when (product) {
    InAppProduct.ACCIDENT -> {
      InAppPurchaseMocks.accidentForm to InAppPurchaseMocks.accidentQuote()
    }

    InAppProduct.PAYMENT_PROTECTION -> {
      InAppPurchaseMocks.paymentProtectionForm to InAppPurchaseMocks.paymentProtectionQuote()
    }
  }
  InAppPurchaseFormScreen(
    productDisplayName = form.product.displayNameFull,
    fieldCount = form.fields.size,
    navigateUp = navigateUp,
    onLearnMore = dropUnlessResumed { onLearnMore(form.product) },
    onContinue = dropUnlessResumed { onQuoted(form.product.displayNameFull, quote) },
  )
}

@Composable
private fun InAppPurchaseFormScreen(
  productDisplayName: String,
  fieldCount: Int,
  navigateUp: () -> Unit,
  onLearnMore: () -> Unit,
  onContinue: () -> Unit,
) {
  InAppPurchasePlaceholderScreen(
    title = productDisplayName,
    description = "Step 1: product info and $fieldCount form fields",
    navigateUp = navigateUp,
    actions = listOf("Learn more" to onLearnMore, "Get price" to onContinue),
  )
}

@HedvigPreview
@Composable
private fun PreviewInAppPurchaseFormScreen() {
  HedvigTheme {
    Surface {
      InAppPurchaseFormScreen("Olycksfallsförsäkring", 2, {}, {}, {})
    }
  }
}
