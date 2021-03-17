package com.hedvig.app.feature.home

import androidx.test.espresso.intent.Intents.times
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_WITH_MULTIPLE_PSA
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.app.util.MarketRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stub
import com.hedvig.testutil.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ActiveWithMultiplePSAsAndConnectPayment : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_ACTIVE_WITH_MULTIPLE_PSA) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.NO)

    @Test
    fun shouldOpenPSALinksAndConnectPayment() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))
        stubExternalIntents()
        Screen.onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.HomePSAItem>(0) {
                    psaLink { stub() }
                    text {
                        hasText(
                            "COVID-19: Your insurance doesn’t cover trips to certain countries. See full list at UD."
                        )
                    }
                    button {
                        click()
                    }
                    psaLink { intended() }
                }
                childAt<HomeTabScreen.HomePSAItem>(1) {
                    text {
                        hasText(
                            "COVID-19: Your insurance doesn’t cover trips to certain countries. See full list at UD."
                        )
                    }
                    button {
                        click()
                    }
                    psaLink { intended(times(2)) }
                }
                childAt<HomeTabScreen.InfoCardItem>(5) {
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
