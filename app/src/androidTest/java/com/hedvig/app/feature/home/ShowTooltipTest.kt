package com.hedvig.app.feature.home

import android.content.Context
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.home.screens.LoggedInScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShowTooltipTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

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
    val apolloCacheClearRule = ApolloCacheClearRule()

    var lastOpenPrevValue: Long = 0

    @Before
    fun setUp() {
        lastOpenPrevValue =
            context().getSharedPreferences("hedvig_shared_preference", Context.MODE_PRIVATE)
                .getLong("shared_preference_last_open", 0)

        context().getSharedPreferences("hedvig_shared_preference", Context.MODE_PRIVATE).edit()
            .putLong("shared_preference_last_open", 0).commit()
    }

    @Test
    fun shouldShowTooltipAfterThirtyDays() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))
        Screen.onScreen<LoggedInScreen> {
            tooltip {
                isVisible()
            }
        }
    }

    @After
    fun teardown() {
        context().getSharedPreferences("hedvig_shared_preference", Context.MODE_PRIVATE).edit()
            .putLong("shared_preference_last_open", lastOpenPrevValue).commit()
    }
}
