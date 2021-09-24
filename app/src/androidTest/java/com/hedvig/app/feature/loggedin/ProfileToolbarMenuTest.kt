package com.hedvig.app.feature.loggedin

import androidx.test.espresso.IdlingRegistry
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.test.espresso.ApolloIdlingResource
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.R
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ProfileToolbarMenuTest : TestCase() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

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
