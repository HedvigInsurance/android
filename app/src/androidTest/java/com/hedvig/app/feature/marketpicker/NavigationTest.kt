package com.hedvig.app.feature.marketpicker

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.MarketTest
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.marketpicker.screens.MarketSelectedScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import io.mockk.every
import org.junit.Rule
import org.junit.Test

class NavigationTest : MarketTest(Market.SE) {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(MarketingActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_SE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowPickerIfNoMarketIsSelected() = run {
        every {
            marketManager.hasSelectedMarket()
        }.returns(false)

        activityRule.launch()

        onScreen<MarketPickerScreen> {
            picker.isVisible()
        }
    }

    @Test
    fun shouldShowMarketSelectedIfMarketSelected() = run {
        every {
            marketManager.hasSelectedMarket()
        }.returns(true)

        activityRule.launch()

        onScreen<MarketSelectedScreen> {
            loginButton.isVisible()
        }
    }
}
