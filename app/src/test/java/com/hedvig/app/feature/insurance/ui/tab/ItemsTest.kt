package com.hedvig.app.feature.insurance.ui.tab

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsNone
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_WITH_CROSS_SELL
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import org.junit.Test

class ItemsTest {
    @Test
    fun `when no cross-sells are available, should not contain any items referencing cross-sells`() {
        val result = items(INSURANCE_DATA)

        assertThat(result).containsNone(InsuranceModel.CrossSellHeader)
        assertThat(result).containsNoneOfType<InsuranceModel.CrossSellCard>()
    }

    @Test
    fun `when cross-sell are available, should contain cross-sell header and cross-sell`() {
        val result = items(INSURANCE_DATA_WITH_CROSS_SELL)

        assertThat(result).contains(InsuranceModel.CrossSellHeader)
        assertThat(result).containsOfType<InsuranceModel.CrossSellCard>()
    }
}
