package com.hedvig.app.feature.referrals

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KButton
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.ReferralsActivatedActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralsActivatedActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(ReferralsActivatedActivity::class.java)

    @Test
    fun shouldCloseWhenPressingButton() {
        onScreen<ReferralsActivatedScreen> {
            close { click() }
        }

        assertTrue(activityRule.activity.isFinishing)
    }

    class ReferralsActivatedScreen : Screen<ReferralsActivatedScreen>() {
        val close = KButton { withId(R.id.close) }
    }
}
