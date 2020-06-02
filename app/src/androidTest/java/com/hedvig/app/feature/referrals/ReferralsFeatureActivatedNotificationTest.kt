package com.hedvig.app.feature.referrals

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.FeaturesQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralsFeatureActivatedNotificationTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Test
    fun shouldOpenLoggedInScreenWithReferralsShownWhenOpeningReferralsFeatureActivatedNotification() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains("Features")) {
                        return MockResponse().setBody(FEATURES_DATA.toJson())
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
                invites { isVisible() }
            }
        }
    }

    companion object {
        private val FEATURES_DATA = FeaturesQuery.Data(
            member = FeaturesQuery.Member(
                features = listOf(
                    Feature.KEYGEAR,
                    Feature.REFERRALS
                )
            )
        )
    }
}
