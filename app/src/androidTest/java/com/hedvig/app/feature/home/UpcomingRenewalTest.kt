package com.hedvig.app.feature.home

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_UPCOMING_RENEWAL
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class UpcomingRenewalTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_UPCOMING_RENEWAL) },
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenPSALinksAndConnectPayment() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))
        stubExternalIntents()
        Screen.onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.UpcomingRenewal>(3) {
                    button {
                        hasText(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                        click()
                    }
                    link { intended() }
                }
            }
        }
    }
}

