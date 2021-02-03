package com.hedvig.app.feature.marketpicker

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.marketManagerModule
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
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

    private val mockMarketManager = mockk<MarketManager>(relaxed = true)

    @get:Rule
    val mockMarketManagerRule = KoinMockModuleRule(
        listOf(marketManagerModule),
        listOf(module { single { mockMarketManager } })
    )

    @Test
    fun shouldNotPreselectMarketWhenUserIsInUnknownGeo() = run {
        every {
            mockMarketManager.hasSelectedMarket()
        }.returns(false)

        every {
            mockMarketManager.market
        }.returns(null)

        every {
            mockMarketManager.enabledMarkets
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
