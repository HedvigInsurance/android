package com.hedvig.app.feature.splash

import com.hedvig.android.owldroid.graphql.ContractStatusQuery
import com.hedvig.app.SplashActivity
import com.hedvig.app.service.LoginStatusService
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(SplashActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ContractStatusQuery.QUERY_DOCUMENT to apolloResponse { internalServerError() }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val loginStatusService = mockk<LoginStatusService>(relaxed = true)

    @Test
    fun shouldNotCrashOnNetworkError() = run {
        every { loginStatusService.isLoggedIn }.returns(true)
        activityRule.launch()
    }
}
