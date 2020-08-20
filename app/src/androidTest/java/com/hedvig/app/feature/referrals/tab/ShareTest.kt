package com.hedvig.app.feature.referrals.tab

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.COMPLEX_REFERRAL_CODE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_COMPLEX_CODE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareTest {

    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

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
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenShareWhenClickingShare() {
        val intent = LoggedInActivity.newInstance(
            ApplicationProvider.getApplicationContext(),
            initialTab = LoggedInTabs.REFERRALS
        )
        activityRule.launchActivity(intent)

        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )

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
