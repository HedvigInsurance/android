package com.hedvig.app.feature.embark

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbarkActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun showsSpinnerWhileLoading() {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )
        onScreen<EmbarkScreen> {
            spinner {
                isVisible()
            }
        }
    }

    @Test
    fun endsActivityIfNoStoryNameIsProvided() {
        activityRule.launchActivity(null)
        assertTrue(activityRule.activity.isFinishing)
        assertTrue(activityRule.activityResult.resultCode == Activity.RESULT_CANCELED)
    }
}
