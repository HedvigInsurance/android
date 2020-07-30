package com.hedvig.app.feature.offer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoExistingInsuranceTest {

    @get:Rule
    val activityRule = ActivityTestRule(OfferActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.OPERATION_NAME to { OFFER_DATA_SWEDISH_APARTMENT }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldNotShowSwitcherSectionWhenUserHasNoExistingInsurance() {
        activityRule.launchActivity(null)

        onScreen<OfferScreen> {
            scroll {
                hasSize(6)
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.START_DATE_TODAY)
                        click()
                    }
                }
            }
        }
        onScreen<ChangeDateSheet> {
            autoSetDate { hasText(R.string.ACTIVATE_TODAY_BTN) }
        }
    }
}

