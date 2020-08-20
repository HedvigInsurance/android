package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Semaphore

@RunWith(AndroidJUnit4::class)
class ErrorTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorWhenAnErrorOccurs() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                var shouldFailureSemaphore = true
                val semaphore = Semaphore(1)

                override fun dispatch(request: RecordedRequest): MockResponse {
                    semaphore.acquire()
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        semaphore.release()
                        return MockResponse().setBody(
                            LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED.toJson()
                        )
                    }

                    if (body.contains(ReferralsQuery.OPERATION_NAME.name())) {
                        if (shouldFailureSemaphore) {
                            shouldFailureSemaphore = false
                            semaphore.release()
                            return MockResponse().setBody(ERROR_JSON)
                        }

                        semaphore.release()
                        return MockResponse().setBody(
                            REFERRALS_DATA_WITH_NO_DISCOUNTS.toJson()
                        )
                    }

                    semaphore.release()
                    return MockResponse()
                }
            }

            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            onScreen<ReferralTabScreen> {
                share { isGone() }
                recycler {
                    hasSize(2)
                    childAt<ReferralTabScreen.ErrorItem>(1) {
                        errorTitle { isVisible() }
                        errorParagraph { isVisible() }
                        retry {
                            isVisible()
                            click()
                        }
                    }
                    hasSize(3)
                    childAt<ReferralTabScreen.HeaderItem>(1) {
                        discountPerMonthPlaceholder { isGone() }
                        newPricePlaceholder { isGone() }
                        discountPerMonth { isGone() }
                        newPrice { isGone() }
                        discountPerMonthLabel { isGone() }
                        newPriceLabel { isGone() }
                        emptyHeadline { isVisible() }
                        emptyBody { isVisible() }
                        otherDiscountBox { isGone() }
                    }
                    childAt<ReferralTabScreen.CodeItem>(2) {
                        placeholder { isGone() }
                        code {
                            isVisible()
                            hasText("TEST123")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ERROR_JSON =
            """{"data": null, "errors": [{"message": "example message"}]}"""
    }
}
