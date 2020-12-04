package com.hedvig.app.feature.splash

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
    @get:Rule
    val activityRule = IntentsTestRule(SplashActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ContractStatusQuery.QUERY_DOCUMENT to apolloResponse { internalServerError() }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private var previousLoginStatus: Boolean = false

    @Before
    fun setup() {
        previousLoginStatus = context().isLoggedIn()
        context().setIsLoggedIn(false)
    }

    @Test
    fun shouldNotCrashOnNetworkError() = run {
        activityRule.launchActivity(null)
        onScreen<SplashScreen> {
            marketing { stub() }
        }
    }

    @After
    fun teardown() {
        context().setIsLoggedIn(previousLoginStatus)
    }

    class SplashScreen : Screen<SplashScreen>() {
        val marketing = KIntent { hasComponent(MarketingActivity::class.java.name) }
    }
}
