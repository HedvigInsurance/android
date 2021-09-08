package com.hedvig.app.feature.insurance.ui.detail

import assertk.assertThat
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.yourInfoItems
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_UPCOMING_AGREEMENT
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import org.junit.Test

class YourInfoItemsTest {
    @Test
    fun `when there is no upcoming agreement, should show no upcoming agreement card`() {
        val result = yourInfoItems(INSURANCE_DATA.contracts[0], false)

        assertThat(result.topItems).containsNoneOfType<YourInfoModel.PendingAddressChange>()
    }

    @Test
    fun `when there is an upcoming agreement, should show an upcoming agreement card`() {
        val result = yourInfoItems(INSURANCE_DATA_UPCOMING_AGREEMENT.contracts[0], false)

        assertThat(result.topItems).containsOfType<YourInfoModel.PendingAddressChange>()
    }

    @Test
    fun `when moving flow is not enabled, should not show move button`() {
        val result = yourInfoItems(INSURANCE_DATA.contracts[0], false)

        assertThat(result.bottomItems).containsNoneOfType<YourInfoModel.ChangeAddressButton>()
    }

    @Test
    fun `when moving flow is enabled, should show move button`() {
        val result = yourInfoItems(INSURANCE_DATA.contracts[0], true)

        assertThat(result.bottomItems).containsOfType<YourInfoModel.ChangeAddressButton>()
    }
}
