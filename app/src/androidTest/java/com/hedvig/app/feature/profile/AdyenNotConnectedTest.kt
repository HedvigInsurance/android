package com.hedvig.app.feature.profile

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_ADYEN_NOT_CONNECTED
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class AdyenNotConnectedTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse { success(LOGGED_IN_DATA_WITH_REFERRALS_ENABLED) },
        ProfileQuery.QUERY_DOCUMENT to apolloResponse { success(PROFILE_DATA_ADYEN_NOT_CONNECTED) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.NO)

    @Test
    fun shouldShowCardNotConnected() = run {
        activityRule.launch(
            LoggedInActivity.newInstance(
                context(),
                initialTab = LoggedInTabs.PROFILE
            )
        )

        ProfileTabScreen {
            recycler {
                childAt<ProfileTabScreen.Row>(3) {
                    caption {
                        hasText(R.string.Card_Not_Connected, defaultAmount)
                    }
                }
            }
        }
    }
}
