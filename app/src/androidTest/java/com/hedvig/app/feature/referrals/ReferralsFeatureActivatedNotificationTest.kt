package com.hedvig.app.feature.referrals

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class ReferralsFeatureActivatedNotificationTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldOpenLoggedInScreenWithReferralsShownWhenOpeningReferralsFeatureActivatedNotification() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(
                            LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED.toJson()
                        )
                    }

                    return MockResponse()
                }
            }

            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            onScreen<ReferralScreen> {
                recycler { isVisible() }
            }
        }
    }
}
