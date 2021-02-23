package com.hedvig.app.feature.marketpicker

import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.data.MarketingRepository
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.marketpicker.screens.MarketSelectedScreen
import com.hedvig.app.marketingRepositoryModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class NavigationTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(MarketingActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_SE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val mockRepository = mockk<MarketingRepository>(relaxed = true)

    @get:Rule
    val mock = KoinMockModuleRule(
        listOf(marketingRepositoryModule),
        listOf(module { single { mockRepository } })
    )

    @Test
    fun shouldShowPickerIfNoMarketIsSelected() = run {
        every {
            mockRepository.hasSelectedMarket()
        }.returns(false)

        activityRule.launch()

        MarketPickerScreen {
            picker.isVisible()
        }
    }

    @Test
    fun shouldShowMarketSelectedIfMarketSelected() = run {
        every {
            mockRepository.hasSelectedMarket()
        }.returns(true)

        activityRule.launch()

        MarketSelectedScreen {
            loginButton.isVisible()
        }
    }
}
