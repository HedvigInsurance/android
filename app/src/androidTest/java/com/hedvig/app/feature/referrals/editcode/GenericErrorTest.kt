package com.hedvig.app.feature.referrals.editcode

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.hasError
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenericErrorTest {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

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
                    hasError(R.string.referrals_change_code_sheet_general_error)
                }
            }
        }
    }

    companion object {
        private const val ERROR_JSON =
            """{"data": null, "errors": [{"message": "example message"}]}"""
    }
}
