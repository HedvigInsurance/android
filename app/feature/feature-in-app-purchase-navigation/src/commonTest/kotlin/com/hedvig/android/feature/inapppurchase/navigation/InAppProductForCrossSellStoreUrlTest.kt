package com.hedvig.android.feature.inapppurchase.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class InAppProductForCrossSellStoreUrlTest {
  @Test
  fun `redirect link resolves the product from its next parameter`() {
    val storeUrl = "https://dev.hedvigit.com/api/redirect/recommend?attributed_to=HEDVIG&initiated_from=APP" +
      "&attribution.userFlow=INSURANCES&next=%2Fse-en%2Finsurances%2Fpayment-protection&requiresAuthorization=true"
    assertThat(inAppProductForCrossSellStoreUrl(storeUrl)).isEqualTo(InAppProduct.PAYMENT_PROTECTION)
  }

  @Test
  fun `direct product page resolves the product from its path`() {
    assertThat(inAppProductForCrossSellStoreUrl("https://www.hedvig.com/se/forsakringar/olycksfallsforsakring/"))
      .isEqualTo(InAppProduct.ACCIDENT)
    assertThat(inAppProductForCrossSellStoreUrl("https://www.hedvig.com/se/forsakringar/trygghetsforsakring?a=b"))
      .isEqualTo(InAppProduct.PAYMENT_PROTECTION)
  }

  @Test
  fun `other products are not bought in the app`() {
    val storeUrl = "https://dev.hedvigit.com/api/redirect/recommend?next=%2Fse-en%2Finsurances%2Fpet-insurance"
    assertThat(inAppProductForCrossSellStoreUrl(storeUrl)).isNull()
  }
}
