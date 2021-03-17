package com.hedvig.app.feature.loggedin

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity.Companion.EXTRA_IS_FROM_ONBOARDING
import com.hedvig.app.testdata.feature.loggedin.WELCOME_DATA_ONE_PAGE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class PostSignTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        WelcomeQuery.QUERY_DOCUMENT to apolloResponse {
            success(WELCOME_DATA_ONE_PAGE)
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenWelcomeWhenNavigatingFromOnboarding() = run {
        activityRule.launch(
            LoggedInActivity.newInstance(context())
                .apply { putExtra(EXTRA_IS_FROM_ONBOARDING, true) }
        )

        onScreen<WelcomeScreen> {
            close {
                isVisible()
                click()
            }
        }
        onScreen<LoggedInScreen> {
            pressBack()
            root { isVisible() }
            bottomTabs {
                hasSelectedItem(R.id.home)
            }
        }
    }
}
