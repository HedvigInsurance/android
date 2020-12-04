package com.hedvig.app.feature.offer

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ExistingNonSwitchableInsuranceTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(OfferActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowSwitcherSectionWhenUserHasExistingSwitchableInsurance() = run {
        activityRule.launchActivity(null)

        onScreen<OfferScreen> {
            scroll {
                hasSize(6)
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.START_DATE_TODAY)
                        click()
                    }
                }
            }
        }

        onScreen<ChangeDateSheet> {
            autoSetDate { hasText(R.string.ACTIVATE_TODAY_BTN) }
        }
    }
}
