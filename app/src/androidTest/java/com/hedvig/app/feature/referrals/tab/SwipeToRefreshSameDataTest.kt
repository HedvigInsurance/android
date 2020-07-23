package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeToRefreshSameDataTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
        ReferralsQuery.OPERATION_NAME to { REFERRALS_DATA_WITH_NO_DISCOUNTS }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRefreshDataWhenSwipingDownToRefreshWhenDataHasNotChanged() {
        val intent = LoggedInActivity.newInstance(
            ApplicationProvider.getApplicationContext(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launchActivity(intent)

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

