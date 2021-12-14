package com.hedvig.app.feature.offer.switching

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_DENMARK_BUNDLE_HOME_CONTENTS_TRAVEL_ACCIDENT_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ThreeMixedSwitchableSwitcherTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_DENMARK_BUNDLE_HOME_CONTENTS_TRAVEL_ACCIDENT_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun givenQuoteBundleThreeMixedSwitchablePreviousInsurersShouldShowPreviousInsurerSection() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123", "234", "345")))

        OfferScreen {
            scroll {
                val startIndex = 8
                childAt<OfferScreen.SwitcherItem>(startIndex) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(0))
                    }
                }
                childAt<OfferScreen.SwitcherItem>(startIndex + 1) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(2))
                    }
                }
                childAt<OfferScreen.WarningCard>(startIndex + 2) { isShown() }
                childAt<OfferScreen.SwitcherItem>(startIndex + 3) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(1))
                    }
                }
                childAt<OfferScreen.InfoCard>(startIndex + 4) { isShown() }
            }
        }
    }

    private fun quoteDisplayName(quoteNumber: Int) =
        OFFER_DATA_DENMARK_BUNDLE_HOME_CONTENTS_TRAVEL_ACCIDENT_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
            .quoteBundle
            .quotes[quoteNumber]
            .displayName
}
