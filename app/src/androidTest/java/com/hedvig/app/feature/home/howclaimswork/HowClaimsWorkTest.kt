package com.hedvig.app.feature.home.howclaimswork

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.home.screens.HonestyPledgeSheetScreen
import com.hedvig.app.feature.home.screens.HowClaimsWorkScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class HowClaimsWorkTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenClaimFromHowClaimsWork() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))
        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.HowClaimsWork>(2) {
                    button {
                        hasText(R.string.home_tab_claim_explainer_button)
                        click()
                    }
                }
            }
        }
        onScreen<HowClaimsWorkScreen> {
            button {
                hasText(R.string.claims_explainer_button_next)
                click()
                click()
                hasText(R.string.claims_explainer_button_start_claim)
                click()
            }
        }
        onScreen<HonestyPledgeSheetScreen> {
            chat { stub() }
            claim {
                hasText(R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL)
                click()
            }
            chat { intended() }
        }
    }
}
