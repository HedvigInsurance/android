package com.hedvig.app.feature.insurance.tab

import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasPluralText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TerminatedAndActiveContractsTest {

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
            success(INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowTerminatedContractsRowWhenUserHasTerminatedContracts() {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.INSURANCE
        )
        activityRule.launchActivity(intent)

        onScreen<InsuranceScreen> {
            insuranceRecycler {
                childAt<InsuranceScreen.ContractCard>(1) {
                    firstStatusPill { isGone() }
                }
                childAt<InsuranceScreen.TerminatedContractsHeader>(2) { text { hasText(R.string.insurances_tab_more_title) } }
                childAt<InsuranceScreen.TerminatedContracts>(3) {
                    caption {
                        hasPluralText(R.plurals.insurances_tab_terminated_insurance_subtitile, 1, 1)
                    }
                    click()
                }
            }
            terminatedContractsScreen { intended() }
        }
    }
}
