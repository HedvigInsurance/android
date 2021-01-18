package com.hedvig.app.feature.referrals.moreinfo

import android.content.Intent
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.tab.ReferralTabScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test

class ReferralsInformationActivityTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenInformationActivityWhenClickingMoreInformationAction() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launch(intent)

        onScreen<ReferralTabScreen> {
            moreInfo { click() }
        }

        stubExternalIntents()

        onScreen<ReferralsInformationScreen> {
            body {
                containsText(
                    Money.of(10, "SEK").format(context())
                )
            }
            termsAndConditions { click() }
            termsAndConditionsIntent {
                intended()
            }
        }
    }

    class ReferralsInformationScreen : Screen<ReferralsInformationScreen>() {
        val body = KTextView { withId(R.id.body) }
        val termsAndConditions = KButton { withId(R.id.termsAndConditions) }
        val termsAndConditionsIntent = KIntent {
            hasAction(Intent.ACTION_VIEW)
            hasData("https://www.example.com")
        }
    }
}
