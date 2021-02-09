package com.hedvig.app.feature.embark

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import com.hedvig.app.feature.embark.screens.MoreOptionsScreen
import com.hedvig.app.feature.embark.screens.ZignSecScreen
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.onboarding.MEMBER_ID_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class MoreOptionsTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(MoreOptionsActivity::class.java)

    var shouldFail = true

    val mockMarketProvider = mockk<MarketProvider>(relaxed = true)

    init {
        every { mockMarketProvider.market } returns Market.NO
    }

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketProviderModule),
        listOf(module { single { mockMarketProvider } })
    )

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        MemberIdQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(MEMBER_ID_DATA)
            }
        }
    )

    @Test
    fun openMoreOptionsActivity() = run {
        activityRule.launch(MoreOptionsActivity.newInstance(context()))
        onScreen<MoreOptionsScreen> {
            recycler {
                childAt<MoreOptionsScreen.Row>(1) {
                    info {
                        click()
                        hasText("1234567890")
                    }
                }
            }
        }
    }

    @Test
    fun loginButtonShouldOpenLoginMethod() {
        val newInstance = MoreOptionsActivity.newInstance(context())
        activityRule.launch(newInstance)

        onScreen<MoreOptionsScreen> {
            loginButton {
                click()
            }
        }

        onScreen<ZignSecScreen> {
            webView {
                isVisible()
            }
        }
    }
}
