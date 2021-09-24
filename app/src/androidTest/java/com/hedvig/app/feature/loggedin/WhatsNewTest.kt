package com.hedvig.app.feature.loggedin

import androidx.test.espresso.IdlingRegistry
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.test.espresso.ApolloIdlingResource
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.R
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject

@HiltAndroidTest
class WhatsNewTest : TestCase() {

    val hiltRule = HiltAndroidRule(this)
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @Inject
    lateinit var apolloClient: ApolloClient

    @Rule
    fun chain(): RuleChain = RuleChain
        .outerRule(hiltRule)
        .around(activityRule)
        // .around(ApolloCacheClearRule())
        .around(
            ApolloMockServerRule(
                LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
                    success(LOGGED_IN_DATA_WITH_REFERRALS_ENABLED)
                },
                WhatsNewQuery.QUERY_DOCUMENT to apolloResponse {
                    success(WHATS_NEW)
                }
            )
        )

    @Before
    fun init() {
        hiltRule.inject()

        val idlingResource =
            ApolloIdlingResource.create("ApolloIdlingResource", apolloClient)
        IdlingRegistry
            .getInstance()
            .register(idlingResource)
    }

    @Test
    fun shouldOpenWhatsNew() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))

        onScreen<WelcomeScreen> {
            pressBack()
        }
        onScreen<LoggedInScreen> {
            root { isVisible() }
            bottomTabs {
                hasSelectedItem(R.id.home)
            }
        }
    }
}
