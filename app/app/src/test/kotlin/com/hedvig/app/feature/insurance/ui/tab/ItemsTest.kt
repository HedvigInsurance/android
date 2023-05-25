package com.hedvig.app.feature.insurance.ui.tab

import assertk.assertThat
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_WITH_CROSS_SELL
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import org.junit.Test

class ItemsTest {
  @Test
  fun `when no cross-sells are available, should not contain any items referencing cross-sells`() {
    val result = buildInsuranceModelItems(
      insurances = INSURANCE_DATA,
      crossSells = emptyList(),
    )

    assertThat(result).containsNoneOfType<InsuranceModel.CrossSellHeader>()
    assertThat(result).containsNoneOfType<InsuranceModel.CrossSellCard>()
  }

  @Test
  fun `when cross-sell are available, should contain cross-sell header and cross-sell`() {
    val result = buildInsuranceModelItems(
      insurances = INSURANCE_DATA_WITH_CROSS_SELL,
      crossSells = listOf(
        CrossSellData(
          id = "123",
          title = "",
          description = "",
          storeUrl = "",
          backgroundUrl = "",
          backgroundBlurHash = "",
          about = "",
          perils = emptyList(),
          terms = emptyList(),
          highlights = emptyList(),
          faq = emptyList(),
          insurableLimits = emptyList(),
        ),
      ),
    )

    assertThat(result).containsOfType<InsuranceModel.CrossSellHeader>()
    assertThat(result).containsOfType<InsuranceModel.CrossSellCard>()
  }
}
