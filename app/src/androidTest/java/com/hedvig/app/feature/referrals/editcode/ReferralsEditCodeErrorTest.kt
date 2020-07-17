package com.hedvig.app.feature.referrals.editcode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class ReferralsEditCodeErrorTest : KoinComponent {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowErrorWhenNetworkErrorOccurs() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    val bodyAsJson = JSONObject(body)
                    val operationName = bodyAsJson.getString("operationName")

                    if (operationName == UpdateReferralCampaignCodeMutation.OPERATION_NAME.name()) {
                        return MockResponse().setBody(ERROR_JSON)
                    }

                    return super.peek()
                }
            }

            webServer.start(8080)

            activityRule.launchActivity(
                ReferralsEditCodeActivity.newInstance(
                    ApplicationProvider.getApplicationContext(),
                    "TEST123"
                )
            )

            onScreen<ReferralsEditCodeScreen> {
                editLayout {
                    edit {
                        replaceText("EDITEDCODE123")
                    }
                }
                save { click() }
                editLayout {
                    isErrorEnabled()
                    hasError(
                        ApplicationProvider.getApplicationContext<Context>()
                            .getString(R.string.referrals_change_code_sheet_general_error)
                    )
                }
            }
        }
    }

    companion object {
        private const val ERROR_JSON =
            """{"data": null, "errors": [{"message": "example message"}]}"""
    }
}
