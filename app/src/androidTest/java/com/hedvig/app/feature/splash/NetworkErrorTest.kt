package com.hedvig.app.feature.splash

import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.testutil.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(SplashActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ContractStatusQuery.QUERY_DOCUMENT to apolloResponse { internalServerError() }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    private var previousLoginStatus: Boolean = false

    @Before
    fun setup() {
        previousLoginStatus = context().isLoggedIn()
        context().setIsLoggedIn(false)
    }

    @Test
    fun shouldNotCrashOnNetworkError() = run {
        activityRule.launch()
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
