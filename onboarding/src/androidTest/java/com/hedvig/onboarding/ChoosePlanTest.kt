package com.hedvig.onboarding

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.stub
import com.hedvig.onboarding.chooseplan.ChoosePlanActivity
import com.hedvig.testutil.ApolloLocalServerRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChoosePlanTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(ChoosePlanActivity::class.java)

     @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ChoosePlanQuery.QUERY_DOCUMENT to apolloResponse { success(CHOOSE_PLAN_DATA) }
    )

    @Test
    fun chooseContentsPlan() = run {
        activityRule.launch()
        com.hedvig.onboarding.screens.ChoosePlanScreen {
            contents { stub() }
            recycler {
                childAt<com.hedvig.onboarding.screens.ChoosePlanScreen.Card>(1) {
                    radioButton {
                        isNotChecked()
                        click()
                        isChecked()
                    }
                }
            }
            continueButton { click() }
            contents { intended() }
        }
    }

    @Test
    fun chooseTravelPlan() = run {
        activityRule.launch()
        com.hedvig.onboarding.screens.ChoosePlanScreen {
            travel { stub() }
            recycler {
                childAt<com.hedvig.onboarding.screens.ChoosePlanScreen.Card>(2) {
                    radioButton {
                        isNotChecked()
                        click()
                        isChecked()
                    }
                }
            }
            continueButton { click() }
            travel { intended() }
        }
    }
}
