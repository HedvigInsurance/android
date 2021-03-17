package com.hedvig.onboarding

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class EmbarkActivityTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun showsSpinnerWhileLoading() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
            )
        )
        onScreen<EmbarkScreen> {
            spinner {
                isVisible()
            }
        }
    }
}
