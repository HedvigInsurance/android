package com.hedvig.app.feature.referrals.editcode

import androidx.test.espresso.Espresso
import androidx.test.rule.ActivityTestRule
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

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

