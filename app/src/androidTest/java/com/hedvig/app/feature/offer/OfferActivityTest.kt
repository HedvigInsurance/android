package com.hedvig.app.feature.offer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.util.apolloMockServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class OfferActivityTest : KoinComponent {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(OfferActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowSwitcherSectionWhenUserHasExistingSwitchableInsurance() {
        apolloMockServer(
            OfferQuery.OPERATION_NAME.name() to OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
        ).use { webServer ->
            webServer.start(8080)

            activityRule.launchActivity(null)

            onScreen<OfferScreen> {
                scroll {
                    childAt<OfferScreen.HeaderItem>(0) {
                        startDate { hasText(R.string.ACTIVATE_INSURANCE_END_BTN) }
                    }
                }
            }
        }
    }
}
