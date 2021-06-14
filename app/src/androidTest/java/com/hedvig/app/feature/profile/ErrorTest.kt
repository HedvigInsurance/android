package com.hedvig.app.feature.profile

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.profile.screens.ProfileScreen
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ErrorTest : TestCase() {
    val intent = LoggedInActivity.newInstance(
        context(),
        initialTab = LoggedInTabs.PROFILE
    )

    @get:Rule
    var activityRule = ActivityScenarioRule<LoggedInActivity>(intent)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED)
        },
        ProfileQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(PROFILE_DATA)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldReload() = run {
        ProfileScreen {
            recycler {
                childAt<ProfileScreen.Error>(0) {
                    retry { click() }
                }
            }
            recycler {
                childAt<ProfileScreen.Title>(0) {
                    header {
                        hasText(R.string.PROFILE_TITLE)
                    }
                }
            }
        }
    }
}
