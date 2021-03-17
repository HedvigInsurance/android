package com.hedvig.app.feature.offer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_HOUSE
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class HouseTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(OFFER_DATA_SWEDISH_HOUSE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldNotCrashWhenShowingHouseOffer() = run {
        activityRule.launch()

        onScreen<OfferScreen> {
            scroll {
                flakySafely {
                    childAt<OfferScreen.Facts>(2) {
                        expandableContent { click() }
                        additionalBuildings {
                            isVisible()
                        }
                    }
                }
            }
        }
    }
}
