package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
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
class ReferralTabLoadingTest : KoinTest {
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
    fun shouldShowLoadingWhenDataHasNotLoaded() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(LOGGED_IN_DATA.toJson())
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
                share { isGone() }
                recycler {
                    firstChild<ReferralScreen.HeaderItem> {
                        discountPerMonthPlaceholder { isVisible() }
                        newPricePlaceholder { isVisible() }
                        discountPerMonth { isGone() }
                        newPrice { isGone() }
                        discountPerMonthLabel { isVisible() }
                        newPriceLabel { isVisible() }
                        emptyHeadline { isGone() }
                        emptyBody { isGone() }
                    }
                    childAt<ReferralScreen.CodeItem>(1) {
                        placeholder { isVisible() }
                        code { isGone() }
                    }
                    childAt<ReferralScreen.InvitesHeaderItem>(2) {
                        isVisible()
                    }
                    childAt<ReferralScreen.ReferralItem>(3) {
                        iconPlaceholder { isVisible() }
                        textPlaceholder { isVisible() }
                        icon { isGone() }
                    }
                }
            }
        }
    }

    companion object {
        private val LOGGED_IN_DATA = LoggedInQuery.Data(
            member = LoggedInQuery.Member(
                features = listOf(
                    Feature.KEYGEAR
                )
            ),
            referralTerms = LoggedInQuery.ReferralTerms(
                url = "https://www.example.com"
            )
        )
    }
}
