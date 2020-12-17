package com.hedvig.app.feature.onboarding

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.feature.onboarding.screens.ChoosePlanScreen
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChoosePlanTest : TestCase() {

    @get:Rule
    val activityRule = IntentsTestRule(ChoosePlanActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ChoosePlanQuery.QUERY_DOCUMENT to apolloResponse { success(CHOOSE_PLAN_DATA) }
    )

    @Test
    fun chooseTravelBundle() = run {
        activityRule.launchActivity(null)
        ChoosePlanScreen {
            recycler {
                childAt<ChoosePlanScreen.Card>(2) {
                    radioButton {
                        isNotChecked()
                        click()
                        isChecked()
                    }
                }
            }
            intent { stub() }
            continueButton { click() }
            intent { intended() }
        }
    }
}
