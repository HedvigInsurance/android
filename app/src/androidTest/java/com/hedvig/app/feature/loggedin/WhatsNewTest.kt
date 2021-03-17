package com.hedvig.app.feature.loggedin

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class WhatsNewTest : TestCase() {
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
        WhatsNewQuery.QUERY_DOCUMENT to apolloResponse {
            success(WHATS_NEW)
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenWhatsNew() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))

        onScreen<WelcomeScreen> {
            close {
                isVisible()
                click()
            }
        }
        onScreen<LoggedInScreen> {
            root { isVisible() }
            bottomTabs {
                hasSelectedItem(R.id.home)
            }
        }
    }
}
