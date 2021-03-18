package com.hedvig.onboarding

import com.hedvig.android.owldroid.graphql.MemberIdQuery
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.onboarding.MEMBER_ID_DATA
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.stub
import com.hedvig.onboarding.moreoptions.MoreOptionsActivity
import com.hedvig.onboarding.screens.MoreOptionsScreen
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MoreOptionsTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(MoreOptionsActivity::class.java)

    var shouldFail = true

    @get:Rule
    val marketRule = MarketRule(Market.NO)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        MemberIdQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(MEMBER_ID_DATA)
            }
        }
    )

    @Test
    fun openMoreOptionsActivity() = run {
        activityRule.launch(MoreOptionsActivity.newInstance(context()))
        MoreOptionsScreen {
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

    @Test
    fun loginButtonShouldOpenLoginMethod() {
        val newInstance = MoreOptionsActivity.newInstance(context())
        activityRule.launch(newInstance)

        MoreOptionsScreen {
            authIntent { stub() }

            loginButton { click() }

            authIntent { intended() }
        }
    }
}
