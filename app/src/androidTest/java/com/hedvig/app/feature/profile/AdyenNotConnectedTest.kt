package com.hedvig.app.feature.profile

import androidx.test.rule.ActivityTestRule
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_ADYEN_CONNECTED
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_ADYEN_NOT_CONNECTED
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class AdyenNotConnectedTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        ProfileQuery.QUERY_DOCUMENT to apolloResponse { success(PROFILE_DATA_ADYEN_NOT_CONNECTED) }
    )

    private val marketProvider = mockk<MarketProvider>(relaxed = true)

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketProviderModule),
        listOf(module {
            single { marketProvider }
        })
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowDirectDebitNotConnected() = run {
        every {
            marketProvider.market
        } returns Market.NO

        activityRule.launchActivity(
            LoggedInActivity.newInstance(
                context(),
                initialTab = LoggedInTabs.PROFILE
            )
        )

        ProfileTabScreen {
            recycler {
                childAt<ProfileTabScreen.Row>(3) {
                    caption {
                        hasText(R.string.Card_Not_Connected,
                            Money.of(349, "SEK").format(context()))
                    }
                }
            }
        }
    }
}

