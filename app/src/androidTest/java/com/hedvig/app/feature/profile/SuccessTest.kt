package com.hedvig.app.feature.profile

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.marketManagerModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class SuccessTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse { success(LOGGED_IN_DATA_WITH_REFERRALS_ENABLED) },
        ProfileQuery.QUERY_DOCUMENT to apolloResponse { success(PROFILE_DATA) }
    )

    private val marketProvider = mockk<MarketManager>(relaxed = true)

    @get:Rule
    val koinMockModuleRule = KoinMockModuleRule(
        listOf(marketManagerModule),
        listOf(
            module {
                single { marketProvider }
            }
        )
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldSuccessfullyLoadProfileTab() = run {
        every {
            marketProvider.market
        } returns Market.SE

        activityRule.launch(
            LoggedInActivity.newInstance(
                context(),
                initialTab = LoggedInTabs.PROFILE
            )
        )

        ProfileTabScreen {
            recycler {
                childAt<ProfileTabScreen.Title>(0) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Row>(1) {
                    caption { hasText("Test Testerson") }
                }
                childAt<ProfileTabScreen.Row>(2) {
                    caption { hasText("Example Charity") }
                }
                childAt<ProfileTabScreen.Row>(3) {
                    caption {
                        hasText(R.string.Direct_Debit_Not_Connected, defaultAmount)
                    }
                }
                childAt<ProfileTabScreen.Subtitle>(4) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Row>(5) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Row>(6) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Logout>(7) {
                    isVisible()
                }
            }
        }
    }
}
