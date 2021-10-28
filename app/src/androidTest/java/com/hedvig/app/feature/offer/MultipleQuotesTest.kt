package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.screen.FAQBottomSheetScreen
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.screen.QuoteDetailScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.feature.perils.PerilRecyclerItem
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MultipleQuotesTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun givenAnyQuoteBundleWithFAQItemsShouldShowFAQSection() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123", "234")))

        OfferScreen {
            scroll {
                scrollToEnd() // Ensure that the view is not laid out behind system UI and hence not interactable
                childAt<OfferScreen.FAQ>(6) {
                    title { isVisible() }
                    faqRow(0) {
                        hasText(firstFaqRowText)
                        click()
                    }
                }
            }
        }

        FAQBottomSheetScreen {
            title { hasText(firstFaqRowText) }
            body { hasText(firstFaqRowBody) }
        }
    }

    private val firstFaqRowText = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
        .quoteBundle
        .frequentlyAskedQuestions[0]
        .headline!!

    private val firstFaqRowBody = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
        .quoteBundle
        .frequentlyAskedQuestions[0]
        .body!!

    @Test
    fun givenBundleWithMultipleQuotesShouldShowQuoteDetailScreens() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123", "234")))

        OfferScreen {
            scroll {
                scrollToEnd() // Ensure that the view is not laid out behind system UI and hence not interactable
                childAt<OfferScreen.QuoteDetail>(4) {
                    text { hasText(homeContentsDisplayName) }
                    click()
                }
            }
        }

        QuoteDetailScreen {
            toolbar { hasTitle(homeContentsDisplayName) }
            recycler {
                childAt<PerilRecyclerItem>(1) {
                    label { hasText(homeContentsFirstPerilTitle) }
                }
            }
        }
    }

    private val homeContentsDisplayName = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
        .quoteBundle
        .quotes[0]
        .displayName

    private val homeContentsFirstPerilTitle = OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL
        .quoteBundle
        .quotes[0]
        .contractPerils[0]
        .fragments
        .perilFragment
        .title
}
