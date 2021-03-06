package com.hedvig.app.feature.insurance.tab

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_DANISH_ACCIDENT
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class DanishAccidentContractTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
            )
        },
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                INSURANCE_DATA_DANISH_ACCIDENT
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRenderDanishAccidentContract() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.INSURANCE
        )
        activityRule.launch(intent)

        onScreen<InsuranceScreen> {
            insuranceRecycler {
                childAt<InsuranceScreen.ContractCard>(1) {
                    contractPills {
                        childAt<InsuranceScreen.ContractCard.ContractPill>(0) {
                            text { hasText(R.string.insurance_tab_covers_you_plus_tag, 2) }
                        }
                    }
                }
            }
        }
    }
}
