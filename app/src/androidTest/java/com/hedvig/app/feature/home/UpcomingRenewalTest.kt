package com.hedvig.app.feature.home

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_WITH_MULTIPLE_PSA
import com.hedvig.app.testdata.feature.home.HOME_DATA_UPCOMING_RENEWAL
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stubExternalIntents
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class UpcomingRenewalTest {
    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_UPCOMING_RENEWAL) },
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenPSALinksAndConnectPayment() {
        activityRule.launchActivity(LoggedInActivity.newInstance(context()))
        stubExternalIntents()
        Screen.onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.UpcomingRenewal>(3) {
                    button {
                        hasText(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                        click()
                    }
                    link { intended() }
                }
            }
        }
    }
}

