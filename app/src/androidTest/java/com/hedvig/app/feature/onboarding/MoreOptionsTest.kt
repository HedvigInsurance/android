package com.hedvig.app.feature.onboarding

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.MoreOptionsQuery
import com.hedvig.app.feature.onbarding.ui.MoreOptionsActivity
import com.hedvig.app.feature.onboarding.screens.MoreOptionsScreen
import com.hedvig.app.testdata.feature.onboarding.MORE_OPTIONS_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MoreOptionsTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(MoreOptionsActivity::class.java, false, false)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        MoreOptionsQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(MORE_OPTIONS_DATA)
            }
        }
    )

    @Test
    fun openMoreOptionsActivity() = run {
        activityRule.launchActivity(null)
        onScreen<MoreOptionsScreen> {
            recycler {
                childAt<MoreOptionsScreen.Row>(1) {
                    info {
                        click()
                        hasText("1234567890")
                    }
                }
            }
        }
    }
}
