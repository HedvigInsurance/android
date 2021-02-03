package com.hedvig.app.feature.marketpicker

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.marketpicker.screens.MarketSelectedScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.marketManagerModule
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

    private val mockMarketManager = mockk<MarketManager>(relaxed = true)

    @get:Rule
    val mock = KoinMockModuleRule(
        listOf(marketManagerModule),
        listOf(module { single { mockMarketManager } })
    )

    @Test
    fun shouldShowPickerIfNoMarketIsSelected() = run {
        every {
            mockMarketManager.hasSelectedMarket()
        }.returns(false)

        activityRule.launch()

        onScreen<MarketPickerScreen> {
            picker.isVisible()
        }
    }

    @Test
    fun shouldShowMarketSelectedIfMarketSelected() = run {
        every {
            mockMarketManager.hasSelectedMarket()
        }.returns(true)

        every {
            mockMarketManager.market
        }.returns(Market.SE)

        activityRule.launch()

        onScreen<MarketSelectedScreen> {
            loginButton.isVisible()
        }
    }
}
