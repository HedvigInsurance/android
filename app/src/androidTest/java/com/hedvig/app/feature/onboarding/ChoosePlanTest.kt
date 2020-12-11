package com.hedvig.app.feature.onboarding

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.feature.onbarding.ui.ChoosePlanActivity
import com.hedvig.app.feature.onboarding.screens.ChoosePlanScreen
import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChoosePlanTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ChoosePlanActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        ChoosePlanQuery.QUERY_DOCUMENT to apolloResponse { success(CHOOSE_PLAN_DATA) }
    )

    @Test
    fun chooseTravelBundle() = run {
        activityRule.launchActivity(null)
        Screen.onScreen<ChoosePlanScreen> {
            recycler {
                childAt<ChoosePlanScreen.Card>(2) {
                    radioButton {
                        isNotChecked()
                        click()
                        isChecked()
                    }
                }
            }
        }
    }
}
