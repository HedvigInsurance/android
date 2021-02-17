package com.hedvig.app.feature.onboarding

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.feature.onboarding.screens.ChoosePlanScreen
import com.hedvig.chooseplan.ChoosePlanActivity
import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChoosePlanTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(com.hedvig.chooseplan.ChoosePlanActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ChoosePlanQuery.QUERY_DOCUMENT to apolloResponse { success(CHOOSE_PLAN_DATA) }
    )

    @Test
    fun chooseContentsPlan() = run {
        activityRule.launch()
        ChoosePlanScreen {
            contents { stub() }
            recycler {
                childAt<ChoosePlanScreen.Card>(1) {
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
        ChoosePlanScreen {
            travel { stub() }
            recycler {
                childAt<ChoosePlanScreen.Card>(2) {
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
