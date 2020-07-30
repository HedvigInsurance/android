package com.hedvig.app.feature.offer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExistingSwitchableInsuranceTest {

    @get:Rule
    val activityRule = ActivityTestRule(OfferActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowSwitcherSectionWhenUserHasExistingSwitchableInsurance() {
        activityRule.launchActivity(null)

        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.SwitcherItem>(5) { title { containsText("Annat Försäkringsbolag") } }
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        hasText(R.string.ACTIVATE_INSURANCE_END_BTN)
                        click()
                    }
                }
            }
        }
        onScreen<ChangeDateSheet> {
            autoSetDate { hasText(R.string.ACTIVATE_INSURANCE_END_BTN) }
        }
    }
}
