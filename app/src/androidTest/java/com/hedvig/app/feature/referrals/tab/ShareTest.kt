package com.hedvig.app.feature.referrals.tab

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.COMPLEX_REFERRAL_CODE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_COMPLEX_CODE
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test

class ShareTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_COMPLEX_CODE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenShareWhenClickingShare() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )
        activityRule.launch(intent)

        stubExternalIntents()

        Screen.onScreen<ReferralTabScreen> {
            share {
                isVisible()
                click()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    equalTo(Intent.EXTRA_INTENT),
                    allOf(
                        hasAction(Intent.ACTION_SEND),
                        hasExtra(
                            equalTo(Intent.EXTRA_TEXT),
                            containsString(Uri.encode(COMPLEX_REFERRAL_CODE))
                        )
                    )
                )
            )
        )
    }
}
