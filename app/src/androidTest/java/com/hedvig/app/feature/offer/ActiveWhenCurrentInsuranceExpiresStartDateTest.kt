package com.hedvig.app.feature.offer

import androidx.test.espresso.action.ViewActions.click
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ActiveWhenCurrentInsuranceExpiresStartDateTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldSetDateLabel() = run {
        activityRule.launch()
        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.ACTIVATE_INSURANCE_END_BTN)
                    }
                }
            }
        }
    }
}
