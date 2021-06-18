package com.hedvig.app.feature.offer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class ExistingSwitchableInsuranceTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowSwitcherSectionWhenUserHasExistingSwitchableInsurance() = run {
        activityRule.launch()

        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.SwitcherItem>(5) { title { containsText("Annat Försäkringsbolag") } }
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.ACTIVATE_INSURANCE_END_BTN)
                        click()
                    }
                }
            }
        }
        onScreen<ChangeDateSheet> {
            autoSetDate { hasText(R.string.OFFER_PLAN_EXIRES_TEXT) }
        }
    }
}
