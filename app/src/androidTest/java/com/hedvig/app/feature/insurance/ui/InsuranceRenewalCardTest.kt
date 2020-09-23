package com.hedvig.app.feature.insurance.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.stubExternalIntents
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InsuranceRenewalCardTest {

    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
            )
        },
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                INSURANCE_DATA
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenRenewalInfoLink() {
        val intent = LoggedInActivity.newInstance(
            ApplicationProvider.getApplicationContext(),
            initialTab = LoggedInTabs.INSURANCE
        )
        activityRule.launchActivity(intent)
        stubExternalIntents()
        onScreen<InsuranceScreen> {
            root {
                childAt<InsuranceScreen.InfoCardItem>(1) {
                    title {
                        hasText(R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE)
                    }
                    action {
                        hasText(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                        click()
                    }
                    renewalLink { intended() }
                }
            }
        }
    }
}
