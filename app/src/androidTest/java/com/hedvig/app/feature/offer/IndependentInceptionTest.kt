package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.screen.ChangeDateSheet
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasNrOfChildren
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class IndependentInceptionTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldSetDateLabel() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123")))
        OfferScreen {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDateLabel {
                        hasText(R.string.OFFER_START_DATE_PLURAL)
                    }
                    startDate {
                        hasEmptyText()
                    }
                }
            }
        }
    }

    @Test
    fun shouldHaveMultipleDateViews() = run {
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123")))
        OfferScreen {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        click()
                    }
                }
            }
        }
        ChangeDateSheet {
            changeDateContainer {
                hasNrOfChildren(3)
            }
        }
    }
}
