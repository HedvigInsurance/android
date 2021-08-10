package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.screen.ChangeDateView
import com.hedvig.app.feature.offer.screen.OfferScreen
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER
import com.hedvig.app.testdata.feature.offer.TEST_INSURER_DISPLAY_NAME
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class StartDateFromPreviousInsurerTest : TestCase() {

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
        activityRule.launch(OfferActivity.newInstance(context(), listOf("123")))
        OfferScreen {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.START_DATE_EXPIRES)
                    }
                }
            }
        }
    }

    @Test
    fun switchShouldBeCheckedWhenOpeningBottomSheet() = run {
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
        ChangeDateView {
            switches {
                isChecked()
            }
        }
    }

    @Test
    fun previousInsurerNameShouldShowInSwitch() = run {
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
        val text = context().getString(R.string.OFFER_PLAN_EXIRES_TEXT, TEST_INSURER_DISPLAY_NAME)

        ChangeDateView {
            switches {
                hasText(text)
            }
        }
    }
}
