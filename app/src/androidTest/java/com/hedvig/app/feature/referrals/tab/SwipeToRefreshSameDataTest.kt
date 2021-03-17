package com.hedvig.app.feature.referrals.tab

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SwipeToRefreshSameDataTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldRefreshDataWhenSwipingDownToRefreshWhenDataHasNotChanged() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launch(intent)

        Screen.onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(3)
            }
            swipeToRefresh { swipeDown() }
            recycler { hasSize(3) }
            swipeToRefresh { isNotRefreshing() }
        }
    }
}
