package com.hedvig.app.feature.loggedin

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.referrals.deeplinks.ForeverDeepLinkTest
import com.hedvig.app.testdata.feature.home.HOME_DATA_TERMINATED
import com.hedvig.app.testdata.feature.loggedin.CONTRACT_STATUS_DATA_ONE_TERMINATED_CONTRACT
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.seconds
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Currently malfunctioning.")
class TerminatedTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(SplashActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ContractStatusQuery.QUERY_DOCUMENT to apolloResponse {
            success(CONTRACT_STATUS_DATA_ONE_TERMINATED_CONTRACT)
        },
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(LOGGED_IN_DATA_WITH_REFERRALS_ENABLED)
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse {
            success(HOME_DATA_TERMINATED)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private var wasLoggedIn = false

    @Test
    fun shouldOpenWithHomeTabWhenUserIsNotTerminated() = before {
        wasLoggedIn = context().isLoggedIn()
        context().setIsLoggedIn(false)
    }.after {
        context().setIsLoggedIn(wasLoggedIn)
    }.run {
        activityRule.launch()

        onScreen<ForeverDeepLinkTest.SplashScreen> {
            await atMost 5.seconds untilAsserted {
                animation { doesNotExist() }
            }
        }
        onScreen<LoggedInScreen> {
            root { isVisible() }
            bottomTabs { hasSelectedItem(R.id.home) }
        }
    }
}
