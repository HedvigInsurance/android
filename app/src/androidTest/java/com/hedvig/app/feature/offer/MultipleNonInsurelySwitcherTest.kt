package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultipleNonInsurelySwitcherTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun givenQuoteBundleWithMultipleNonSwitchablePreviousInsurerShouldShowPreviousInsurerSection() = run {
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
            }
        }
    }

    private fun previousInsurerNameForQuote(quoteNumber: Int) =
        OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS
            .quoteBundle
            .quotes[quoteNumber]
            .currentInsurer!!
            .displayName!!

    private fun quoteDisplayName(quoteNumber: Int) =
        OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS
            .quoteBundle
            .quotes[quoteNumber]
            .displayName
}
