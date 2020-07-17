package com.hedvig.app.feature.referrals

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloMockServer
import org.hamcrest.CoreMatchers.not
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
class ReferralsInformationActivityTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldOpenInformationActivityWhenClickingMoreInformationAction() {
        apolloMockServer(
            LoggedInQuery.OPERATION_NAME to LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED

        ).use { webServer ->
            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            onScreen<ReferralScreen> {
                moreInfo { click() }
            }

            intending(not(isInternal())).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    null
                )
            )

            onScreen<ReferralsInformationScreen> {
                body {
                    containsText(
                        Money.of(10, "SEK").format(ApplicationProvider.getApplicationContext())
                    )
                }
                termsAndConditions { click() }
                termsAndConditionsIntent {
                    intended()
                }
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
