package com.hedvig.android.feature.product.inapp.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.serialization.Serializable
import org.junit.Test

class CrossSellProductNavigationTest {
  private class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
    override val entries: MutableList<HedvigNavKey> = initial.toMutableList()
  }

  @Test
  fun `a single product opens its product page`() {
    val backstack = TestBackstack(CallerKey)

    backstack.navigateToCrossSellProduct(crossSell("accident"))

    assertThat(backstack.entries).containsExactly(CallerKey, productKey("accident"))
  }

  @Test
  fun `a generic cross-sell opens the options sheet with each option`() {
    val backstack = TestBackstack(CallerKey)

    backstack.navigateToCrossSellProduct(crossSell("pet", options = listOf(crossSell("dog"), crossSell("cat"))))

    assertThat(backstack.entries).containsExactly(
      CallerKey,
      CrossSellOptionsKey(options = listOf(productKey("dog"), productKey("cat"))),
    )
  }

  @Test
  fun `choosing an option replaces the sheet with the product page`() {
    val sheet = CrossSellOptionsKey(options = listOf(productKey("dog"), productKey("cat")))
    val backstack = TestBackstack(CallerKey, sheet)

    backstack.navigateToSelectedOption(productKey("cat"))

    assertThat(backstack.entries).containsExactly(CallerKey, productKey("cat"))
  }
}

private fun crossSell(id: String, options: List<CrossSell> = emptyList()) = CrossSell(
  id = id,
  title = "$id title",
  subtitle = "$id subtitle",
  storeUrl = "https://$id",
  pillowImageSmall = ImageAsset(id = "$id-small", src = "https://$id-small", description = null),
  pillowImageLarge = ImageAsset(id = "$id-large", src = "https://$id-large", description = null),
  buttonText = "See price",
  options = options,
)

private fun productKey(id: String) = CrossSellProductKey(
  crossSellId = id,
  title = "$id title",
  subtitle = "$id subtitle",
  pillowImageSrc = "https://$id-small",
  storeUrl = "https://$id",
)

@Serializable
private data object CallerKey : HedvigNavKey
