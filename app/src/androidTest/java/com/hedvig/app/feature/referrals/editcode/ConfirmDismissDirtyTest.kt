package com.hedvig.app.feature.referrals.editcode

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.context
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmDismissDirtyTest {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldShowConfirmDismissWhenFormIsDirty() {
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

