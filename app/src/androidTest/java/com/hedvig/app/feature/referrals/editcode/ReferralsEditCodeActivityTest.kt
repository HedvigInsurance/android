package com.hedvig.app.feature.referrals.editcode

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.apolloMockServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class ReferralsEditCodeActivityTest : KoinComponent {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldOpenEditCodeScreenWhenPressingEdit() {
        apolloMockServer(
            LoggedInQuery.OPERATION_NAME to LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED,
            ReferralsQuery.OPERATION_NAME to REFERRALS_DATA_WITH_NO_DISCOUNTS
        ).use { webServer ->
            webServer.start(8080)

            activityRule.launchActivity(
                LoggedInActivity.newInstance(
                    ApplicationProvider.getApplicationContext(),
                    initialTab = LoggedInTabs.REFERRALS
                )
            )

            onScreen<ReferralScreen> {
                recycler {
                    childAt<ReferralScreen.CodeItem>(2) {
                        edit { click() }
                    }
                }
            }

            onScreen<ReferralsEditCodeScreen> {
                edit {
                    hasText("TEST123")
                }
            }
        }
    }

    class ReferralsEditCodeScreen : Screen<ReferralsEditCodeScreen>() {
        val edit = KEditText { withId(R.id.code) }
    }
}
