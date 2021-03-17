package com.hedvig.app.feature.marketpicker

import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketpicker.screens.MarketPickerScreen
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class KnownGeoTest : TestCase() {
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

    private val tracker = mockk<MarketPickerTracker>(relaxed = true)

    @get:Rule
    val koinMockModuleRule = com.hedvig.testutil.KoinMockModuleRule(
        listOf(marketPickerTrackerModule),
        listOf(module { single { tracker } })
    )

    @get:Rule
    val marketRule = MarketRule(Market.SE)

    @Test
    fun shouldPreselectMarketWhenUserIsInSupportedGeoArea() = run {
        every {
            marketRule.marketManager.hasSelectedMarket()
        }.returns(false)

        every {
            marketRule.marketManager.market
        }.returns(null)

        activityRule.launch(MarketingActivity.newInstance(context()))

        MarketPickerScreen {
            picker {
                childAt<MarketPickerScreen.MarketButton>(2) {
                    selectedMarket.hasText(com.hedvig.app.R.string.market_sweden)
                }

                childAt<MarketPickerScreen.ContinueButton>(0) {
                    click()
                }
            }
        }

        verify(exactly = 0) { tracker.selectMarket(any()) }
        verify(exactly = 0) { tracker.selectLocale(any()) }
        verify(exactly = 1) { tracker.submit() }
    }
}
