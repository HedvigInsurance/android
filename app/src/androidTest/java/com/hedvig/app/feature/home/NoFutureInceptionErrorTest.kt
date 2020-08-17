package com.hedvig.app.feature.home

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_IN_FUTURE_NO_INCEPTION
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.hasText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoFutureInceptionErrorTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                success(HOME_DATA_ACTIVE_IN_FUTURE_NO_INCEPTION)
            } else {
                success(HOME_DATA_PENDING)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorWhenUserHasNoFutureInception() {
        activityRule.launchActivity(LoggedInActivity.newInstance(ApplicationProvider.getApplicationContext()))

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.ErrorItem>(0) {
                    retry {
                        click()
                    }
                }
                childAt<HomeTabScreen.BigTextItem>(0) {
                    text {
                        hasText(
                            R.string.home_tab_pending_nonswitchable_welcome_title,
                            "Test"
                        )
                    }
                }
            }
        }
    }
}
