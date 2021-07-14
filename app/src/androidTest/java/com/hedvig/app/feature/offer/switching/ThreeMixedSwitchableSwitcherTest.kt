package com.hedvig.app.feature.offer.switching

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
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
                childAt<OfferScreen.SwitcherItem>(8) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(0))
                    }
                }
                childAt<OfferScreen.SwitcherItem>(9) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(2))
                    }
                }
                childAt<OfferScreen.WarningCard>(10) {
                    title { hasText(R.string.offer_manual_switch_card_title) }
                    body { hasText(R.string.offer_manual_switch_card_body) }
                }
                childAt<OfferScreen.SwitcherItem>(11) {
                    associatedQuote {
                        isVisible()
                        hasText(quoteDisplayName(1))
                    }
                }
                childAt<OfferScreen.InfoCard>(12) {
                    title { hasText(R.string.offer_switch_info_card_title) }
                    body { hasText(R.string.offer_switch_info_card_body) }
                }
            }
        }
    }

    private fun quoteDisplayName(quoteNumber: Int) =
        OFFER_DATA_DENMARK_BUNDLE_HOME_CONTENTS_TRAVEL_ACCIDENT_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE
            .quoteBundle
            .quotes[quoteNumber]
            .displayName
}
