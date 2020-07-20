package com.hedvig.app.feature.referrals.deeplinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.referrals.tab.ReferralTabScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.apolloMockServer
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import org.awaitility.Duration
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class ForeverDeepLinkTest : KoinComponent {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    private var previousLoginStatus = false

    @get:Rule
    val activityRule = ActivityTestRule(SplashActivity::class.java, false, false)

    @Before
    fun setup() {
        previousLoginStatus = ApplicationProvider.getApplicationContext<Context>().isLoggedIn()

        ApplicationProvider
            .getApplicationContext<Context>()
            .setIsLoggedIn(true)

        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldOpenLoggedInActivityOnReferralsTabWhenOpeningForeverDeepLink() {
        apolloMockServer(
            LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
            ReferralsQuery.OPERATION_NAME to { REFERRALS_DATA_WITH_NO_DISCOUNTS }
        ).use { webServer ->
            webServer.start(8080)

            activityRule.launchActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(
                        "https://${ApplicationProvider.getApplicationContext<Context>()
                            .getString(R.string.FIREBASE_LINK_DOMAIN)}/forever"
                    )
                }
            )

            onScreen<SplashScreen> {
                await atMost Duration.FIVE_SECONDS untilAsserted {
                    animation { doesNotExist() }
                }
            }
            onScreen<ReferralTabScreen> {
                recycler {
                    isDisplayed()
                }
            }
        }
    }

    class SplashScreen : Screen<SplashScreen>() {
        val animation = KImageView { withId(R.id.splashAnimation) }
    }

    @After
    fun teardown() {
        ApplicationProvider.getApplicationContext<Context>().setIsLoggedIn(previousLoginStatus)
    }
}
