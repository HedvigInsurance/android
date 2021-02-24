package com.hedvig.app.feature.referrals.deeplinks

import android.content.Intent
import android.net.Uri
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.referrals.tab.ReferralTabScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.seconds
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Causes problems with Espresso")
class ForeverDeepLinkTest : TestCase() {

    private var previousLoginStatus = false

    @get:Rule
    val activityRule = LazyActivityScenarioRule(SplashActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Before
    fun setup() {
        previousLoginStatus = context().isLoggedIn()

        context().setIsLoggedIn(true)
    }

    @Test
    fun shouldOpenLoggedInActivityOnReferralsTabWhenOpeningForeverDeepLink() = run {
        activityRule.launch(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://${context().getString(R.string.FIREBASE_LINK_DOMAIN)}/forever"
                )
            }
        )

        onScreen<SplashScreen> {
            animation { doesNotExist() }
        }
        onScreen<ReferralTabScreen> {
            recycler {
                isDisplayed()
            }
        }
    }

    class SplashScreen : Screen<SplashScreen>() {
        val animation = KImageView { withId(R.id.splashAnimation) }
    }

    @After
    fun teardown() {
        context().setIsLoggedIn(previousLoginStatus)
    }
}
