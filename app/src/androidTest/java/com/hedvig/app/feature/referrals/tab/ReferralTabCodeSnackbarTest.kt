package com.hedvig.app.feature.referrals.tab

import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
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
class ReferralTabCodeSnackbarTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        ApplicationProvider.getApplicationContext<Context>().getSystemService<ClipboardManager>()
            ?.clearPrimaryClip()
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowSnackbarWhenClickingCode() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(
                            LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED.toJson()
                        )
                    }

                    if (body.contains(ReferralsQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(
                            REFERRALS_DATA_WITH_NO_DISCOUNTS.toJson()
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

            Screen.onScreen<ReferralScreen> {
                share { isVisible() }
                recycler {
                    hasSize(3)
                    childAt<ReferralScreen.CodeItem>(2) {
                        placeholder { isGone() }
                        code {
                            isVisible()
                            hasText("TEST123")
                            longClick()
                        }
                    }
                }
                codeCopied {
                    isDisplayed()
                }
            }

            val clipboardContent = ApplicationProvider.getApplicationContext<Context>()
                .getSystemService<ClipboardManager>()?.primaryClip?.getItemAt(0)?.text

            assertThat(clipboardContent).isEqualTo("TEST123")
        }
    }
}
