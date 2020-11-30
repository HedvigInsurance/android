package com.hedvig.app.feature.splash

import androidx.test.rule.ActivityTestRule
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.SplashActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.extensions.isLoggedIn
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(SplashActivity::class.java, false, false)

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
    }

    @After
    fun teardown() {
        context().setIsLoggedIn(previousLoginStatus)
    }
}
