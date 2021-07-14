package com.hedvig.app.feature.offer.switching

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultipleSwitchableSwitcherTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun givenQuoteBundleMultipleSwitchablePreviousInsurersShouldShowPreviousInsurerSection() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123", "234")))

        OfferScreen {
            scroll {
                childAt<OfferScreen.SwitcherItem>(8) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(0))
                    }
                    currentInsurer { hasText(previousInsurerNameForQuote(0)) }
                }
                childAt<OfferScreen.SwitcherItem>(9) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(1))
                    }
                    currentInsurer { hasText(previousInsurerNameForQuote(1)) }
                }
                childAt<OfferScreen.InfoCard>(10) { isShown() }
            }
        }
    }

    private fun previousInsurerNameForQuote(quoteNumber: Int) =
        OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
            .quoteBundle
            .quotes[quoteNumber]
            .currentInsurer!!
            .displayName!!

    private fun quoteDisplayName(quoteNumber: Int) =
        OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE
            .quoteBundle
            .quotes[quoteNumber]
            .displayName
}
