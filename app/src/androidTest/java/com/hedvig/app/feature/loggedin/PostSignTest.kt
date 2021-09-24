package com.hedvig.app.feature.loggedin

import androidx.test.espresso.IdlingRegistry
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.test.espresso.ApolloIdlingResource
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.R
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity.Companion.EXTRA_IS_FROM_ONBOARDING
import com.hedvig.app.testdata.feature.loggedin.WELCOME_DATA_ONE_PAGE
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
import javax.inject.Inject

@HiltAndroidTest
class PostSignTest : TestCase() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

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

    @Inject
    lateinit var apolloClient: ApolloClient

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
    fun shouldOpenWelcomeWhenNavigatingFromOnboarding() = run {
        activityRule.launch(
            LoggedInActivity.newInstance(context())
                .apply { putExtra(EXTRA_IS_FROM_ONBOARDING, true) }
        )

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
