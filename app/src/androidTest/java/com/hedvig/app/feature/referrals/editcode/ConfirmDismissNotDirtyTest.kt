package com.hedvig.app.feature.referrals.editcode

import androidx.test.espresso.Espresso
import androidx.test.rule.ActivityTestRule
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/*
* This test cannot be practically ported to `ActivityScenario` as it causes the test to take 50 seconds to finish.
* Related issue: https://github.com/android/android-test/issues/676
* */
class ConfirmDismissNotDirtyTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldNotShowConfirmDismissWhenFormIsNotDirty() = run {
        activityRule.launchActivity(
            ReferralsEditCodeActivity.newInstance(
                context(),
                "TEST123"
            )
        )

        Espresso.pressBackUnconditionally()

        assertTrue(activityRule.activity.isFinishing)
    }
}
