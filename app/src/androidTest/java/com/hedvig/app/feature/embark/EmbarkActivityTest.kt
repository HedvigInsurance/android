package com.hedvig.app.feature.embark

import android.app.Activity
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EmbarkActivityTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

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

    @Test
    fun endsActivityIfNoStoryNameIsProvided() = run {
        activityRule.launch()
        assertTrue(activityRule.scenario.result.resultCode == Activity.RESULT_CANCELED)
    }
}
