package com.hedvig.app.feature.profile.ui.tab

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotInstanceOf
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProfileQueryDataToProfileUiStateMapperTest {
    private fun sut(
        featureManager: FeatureManager = mockk(relaxed = true),
        marketManager: MarketManager = mockk(relaxed = true),
        localeManager: LocaleManager = mockk(relaxed = true),
    ) = ProfileQueryDataToProfileUiStateMapper(
        featureManager,
        marketManager,
        localeManager,
    )

    @Test
    fun `when payment-feature is not activated, should not show payment-data`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN) } returns false
        val mapper = sut(
            featureManager = featureManager,
        )

        val result = mapper.map(PROFILE_DATA)

        assertThat(result.paymentState).isInstanceOf(PaymentState.DontShow::class)
    }

    @Test
    fun `when payment-feature is activated, should show payment-data`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN) } returns true
        val mapper = sut(
            featureManager = featureManager,
        )

        val result = mapper.map(PROFILE_DATA)

        assertThat(result.paymentState).isInstanceOf(PaymentState.Show::class)
    }

    @Test
    fun `when charity-feature is deactivated, should not show charity-data`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.SHOW_CHARITY) } returns false
        val mapper = sut(
            featureManager = featureManager,
        )

        val result = mapper.map(PROFILE_DATA)

        assertThat(result.charityState).isInstanceOf(CharityState.DontShow::class)
    }

    @Test
    fun `when charity-feature is activated, should show charity-data`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.SHOW_CHARITY) } returns true
        val mapper = sut(
            featureManager = featureManager,
        )

        val result = mapper.map(PROFILE_DATA)

        assertThat(result.charityState).isNotInstanceOf(CharityState.DontShow::class)
    }
}
