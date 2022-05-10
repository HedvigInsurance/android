package com.hedvig.app.feature.home.model

import assertk.assertThat
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.home.HOME_DATA_PAYIN_NEEDS_SETUP
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HomeItemsBuilderTest {
    private fun sut(
        featureManager: FeatureManager = mockk(relaxed = true)
    ) = HomeItemsBuilder(
        featureManager,
    )

    @Test
    fun `when connect payin card-feature is disabled and payin is not connected, should not show connect payin`() =
        runTest {
            val featureManager = mockk<FeatureManager>(relaxed = true)
            coEvery { featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER) } returns false
            val builder = sut(featureManager)

            val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP)

            assertThat(result).containsNoneOfType<HomeModel.ConnectPayin>()
        }

    @Test
    fun `when connect payin card-feature is enabled and payin is not connected, should show connect payin`() =
        runTest {
            val featureManager = mockk<FeatureManager>(relaxed = true)
            coEvery { featureManager.isFeatureEnabled(Feature.CONNECT_PAYIN_REMINDER) } returns true
            val builder = sut(featureManager)

            val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP)

            assertThat(result).containsOfType<HomeModel.ConnectPayin>()
        }

    @Test
    fun `when common claims-feature is disabled, should not show common claims`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.COMMON_CLAIMS) } returns false
        val builder = sut(featureManager)

        val result = builder.buildItems(HOME_DATA_ACTIVE)

        assertThat(result).containsNoneOfType<HomeModel.CommonClaim>()
    }

    @Test
    fun `when common claims-feature is enabled, should show common claims`() = runTest {
        val featureManager = mockk<FeatureManager>(relaxed = true)
        coEvery { featureManager.isFeatureEnabled(Feature.COMMON_CLAIMS) } returns true
        val builder = sut(featureManager)

        val result = builder.buildItems(HOME_DATA_ACTIVE)

        assertThat(result).containsOfType<HomeModel.CommonClaim>()
    }
}
