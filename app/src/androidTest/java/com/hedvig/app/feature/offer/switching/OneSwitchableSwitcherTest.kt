package com.hedvig.app.feature.offer.switching

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class OneSwitchableSwitcherTest : TestCase() {

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
    fun givenQuoteBundleWithOneSwitchablePreviousInsurerShouldShowPreviousInsurerSection() = run {
        activityRule.launch(OfferActivity.newInstance(context()))

        OfferScreen {
            scroll {
                childAt<OfferScreen.SwitcherItem>(12) {
                    associatedQuote { isGone() }
                    currentInsurer { hasText(previousInsurerName) }
                }
                childAt<OfferScreen.InfoCard>(13) { isShown() }
            }
        }
    }

    private val previousInsurerName = OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
        .quoteBundle
        .fragments
        .quoteBundleFragment
        .quotes[0]
        .currentInsurer!!
        .displayName!!
}
