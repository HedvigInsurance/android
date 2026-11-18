package com.hedvig.android.feature.product.inapp.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.feature.product.inapp.data.CrossSellProductInfo
import org.junit.Test

class BuyDestinationTest {
  @Test
  fun `buy opens the purchase flow when the product can be bought in the app`() {
    val state = CrossSellProductUiState.Content(productInfo(inAppProduct = InAppProduct.DOG))

    assertThat(state.buyDestination()).isEqualTo(BuyDestination.InApp(InAppProduct.DOG))
  }

  @Test
  fun `buy opens the web store when the product cannot be bought in the app`() {
    val state = CrossSellProductUiState.Content(productInfo(inAppProduct = null))

    assertThat(state.buyDestination()).isEqualTo(BuyDestination.Web)
  }

  @Test
  fun `buy opens the web store while the product info is not loaded`() {
    assertThat(CrossSellProductUiState.Loading.buyDestination()).isEqualTo(BuyDestination.Web)
    assertThat(CrossSellProductUiState.Failure.buyDestination()).isEqualTo(BuyDestination.Web)
  }

  private fun productInfo(inAppProduct: InAppProduct?) = CrossSellProductInfo(
    heroImage = ImageAsset(id = "hero", src = "", description = null),
    heroIntro = null,
    usps = emptyList(),
    carousel = emptyList(),
    inAppProduct = inAppProduct,
    faq = emptyList(),
  )
}
