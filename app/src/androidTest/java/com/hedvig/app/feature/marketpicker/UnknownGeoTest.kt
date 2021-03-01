package com.hedvig.app.feature.marketpicker

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class UnknownGeoTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(MarketingActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        GeoQuery.QUERY_DOCUMENT to apolloResponse { success(GEO_DATA_FI) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val tracker = mockk<MarketPickerTracker>(relaxed = true)

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketPickerTrackerModule),
        listOf(
            module {
                single { tracker }
            }
        )
    )

    @get:Rule
    val marketRule = MarketRule(Market.SE)

    @Test
    fun shouldPreselectMarketWhenUserIsInUnknownGeo() = run {
        every {
            marketRule.marketManager.hasSelectedMarket()
        }.returns(false)

        every {
            marketRule.marketManager.market
        }.returns(null)

        every {
            marketRule.marketManager.enabledMarkets
        }.returns(
            listOf(
                Market.SE,
                Market.NO
            )
        )

        activityRule.launch(MarketingActivity.newInstance(context()))

        onScreen<MarketPickerScreen> {
            picker {
                childAt<MarketPickerScreen.MarketButton>(2) {
                    selectedMarket.hasText(com.hedvig.app.R.string.market_sweden)
                }

                childAt<MarketPickerScreen.ContinueButton>(0) {
                    isEnabled()
                }
            }
        }
    }
}
