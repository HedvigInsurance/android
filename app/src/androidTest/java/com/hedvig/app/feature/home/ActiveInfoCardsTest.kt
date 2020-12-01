package com.hedvig.app.feature.home

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_WITH_PSA
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.hedvig.app.util.stub
import com.hedvig.app.util.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class ActiveInfoCardsTest : TestCase() {
    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_ACTIVE_WITH_PSA) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val marketProvider = mockk<MarketProvider>(relaxed = true)

    init {
        every { marketProvider.market } returns Market.NO
    }

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketProviderModule),
        listOf(module { single { marketProvider } })
    )

    @Test
    fun shouldShowTitleClaimButtonAndCommonClaimsWhenUserHasOneActiveContract() = run {
        activityRule.launchActivity(LoggedInActivity.newInstance(context()))

        stubExternalIntents()

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.BigTextItem>(1) {
                    text { hasText(R.string.home_tab_welcome_title, "Test") }
                }
                childAt<HomeTabScreen.InfoCardItem>(4) {
                    title { hasText(R.string.info_card_missing_payment_title) }
                    body { hasText(R.string.info_card_missing_payment_body) }
                    connectPayinAdyen { stub() }
                    action {
                        hasText(R.string.info_card_missing_payment_button_text)
                        click()
                    }
                    connectPayinAdyen { intended() }
                }
            }
        }
    }
}
