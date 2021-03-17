package com.hedvig.app.feature.loggedin

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
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

class ProfileToolbarMenuTest : TestCase() {
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
        TriggerClaimChatMutation.QUERY_DOCUMENT to apolloResponse {
            success(TriggerClaimChatMutation.Data(true))
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenChatWhenClickingToolbarActionOnProfileTab() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))

        onScreen<LoggedInScreen> {
            chat { stub() }
            root { isVisible() }
            bottomTabs { setSelectedItem(R.id.profile) }
            openChat {
                isVisible()
                click()
            }
            chat { intended() }
        }
    }
}
