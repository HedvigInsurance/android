package com.hedvig.app.feature.keygear

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.screens.KeyGearScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.keygear.KEY_GEAR_DATA
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class Error : TestCase() {
    val intent = LoggedInActivity.newInstance(
        context(),
        initialTab = LoggedInTabs.KEY_GEAR
    )

    @get:Rule
    var activityRule = ActivityScenarioRule<LoggedInActivity>(intent)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(LOGGED_IN_DATA_WITH_KEY_GEAR_AND_REFERRAL_FEATURE_ENABLED)
        },
        KeyGearItemsQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError(jsonObjectOf("message" to "error"))
            } else {
                success(KEY_GEAR_DATA)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldReload() = run {
        KeyGearScreen {
            reload {
                click()
            }
            header {
                isVisible()
                hasText(R.string.KEY_GEAR_TAB_TITLE)
            }
        }
    }
}
