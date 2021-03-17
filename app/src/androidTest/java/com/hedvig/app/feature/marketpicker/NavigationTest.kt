package com.hedvig.app.feature.marketpicker

import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.marketpicker.screens.MarketSelectedScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.testutil.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import org.junit.Rule
import org.junit.Test

class NavigationTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(MarketingActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_SE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.SE)

    @Test
    fun shouldShowPickerIfNoMarketIsSelected() = run {
        every {
            marketRule.marketManager.hasSelectedMarket()
        }.returns(false)

        activityRule.launch()

        MarketPickerScreen {
            picker.isVisible()
        }
    }

    @Test
    fun shouldShowMarketSelectedIfMarketSelected() = run {
        every {
            marketRule.marketManager.hasSelectedMarket()
        }.returns(true)

        activityRule.launch()

        MarketSelectedScreen {
            loginButton.isVisible()
        }
    }
}
