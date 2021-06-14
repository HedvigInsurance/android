package com.hedvig.app.feature.referrals.deeplinks

import android.content.Intent
import android.net.Uri
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.referrals.tab.ReferralTabScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Causes problems with Espresso")
class ForeverDeepLinkTest : TestCase() {

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

    private val loginStatusService = mockk<LoginStatusService>(relaxed = true)

    @Test
    fun shouldOpenLoggedInActivityOnReferralsTabWhenOpeningForeverDeepLink() = run {
        every { loginStatusService.isLoggedIn }.returns(true)

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
}
