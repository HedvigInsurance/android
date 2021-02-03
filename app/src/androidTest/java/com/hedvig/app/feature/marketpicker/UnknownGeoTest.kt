package com.hedvig.app.feature.marketpicker

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.MarketTest
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class UnknownGeoTest : MarketTest(Market.SE) {
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

    @Test
    fun shouldNotPreselectMarketWhenUserIsInUnknownGeo() = run {
        every {
            marketManager.hasSelectedMarket()
        }.returns(false)

        every {
            marketManager.market
        }.returns(null)

        every {
            marketManager.enabledMarkets
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
                    selectedMarket.hasText("Select market")
                }

                childAt<MarketPickerScreen.ContinueButton>(0) {
                    isDisabled()
                }
            }
        }
    }
}
