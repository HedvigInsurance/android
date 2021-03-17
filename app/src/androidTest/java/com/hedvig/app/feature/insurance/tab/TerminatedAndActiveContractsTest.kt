package com.hedvig.app.feature.insurance.tab

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.hasPluralText
import com.hedvig.testutil.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TerminatedAndActiveContractsTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

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
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowTerminatedContractsRowWhenUserHasTerminatedContracts() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.INSURANCE
        )
        activityRule.launch(intent)

        onScreen<InsuranceScreen> {
            terminatedContractsScreen { stub() }
            insuranceRecycler {
                childAt<InsuranceScreen.ContractCard>(1) {
                    firstStatusPill { isGone() }
                }
                childAt<InsuranceScreen.TerminatedContractsHeader>(2) {
                    text { hasText(R.string.insurances_tab_more_title) }
                }
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
