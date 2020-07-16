package com.hedvig.app.feature.referrals.tab

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.apolloMockServer
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class ReferralTabShareTest : KoinTest {
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
    fun shouldOpenShareWhenClickingShare() {
        apolloMockServer(
            LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
            ReferralsQuery.OPERATION_NAME to { REFERRALS_DATA_WITH_NO_DISCOUNTS }
        ).use { webServer ->
            webServer.start(8080)

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

            Screen.onScreen<ReferralScreen> {
                share {
                    isVisible()
                    click()
                }
                shareIntent { intended() }
            }
        }
    }
}
