package com.hedvig.app.feature.embark

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
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
                storyTitle,
            )
        )
        onScreen<EmbarkScreen> {
            spinner {
                isVisible()
            }
        }
    }
}
