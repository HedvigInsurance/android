package com.hedvig.app.feature.referrals.editcode

import androidx.test.rule.ActivityTestRule
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/*
* This test cannot be practically ported to `ActivityScenario` as it causes the test to take 50 seconds to finish.
* Related issue: https://github.com/android/android-test/issues/676
* */
class ConfirmDismissDirtyTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldShowConfirmDismissWhenFormIsDirty() = run {
        activityRule.launchActivity(
            ReferralsEditCodeActivity.newInstance(
                context(),
                "TEST123"
            )
        )

        onScreen<ReferralsEditCodeScreen> {
            editLayout {
                edit {
                    replaceText("EDITEDCODE123")
                }
            }
            pressBack()
            confirmDismiss {
                isDisplayed()
                negativeButton { click() }
            }
            up { click() }
            confirmDismiss {
                isDisplayed()
                positiveButton { click() }
            }
        }

        assertTrue(activityRule.activity.isFinishing)
    }
}
