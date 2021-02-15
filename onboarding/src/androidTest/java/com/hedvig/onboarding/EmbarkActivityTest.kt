package com.hedvig.onboarding

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.embark.ui.EmbarkActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class EmbarkActivityTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

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
