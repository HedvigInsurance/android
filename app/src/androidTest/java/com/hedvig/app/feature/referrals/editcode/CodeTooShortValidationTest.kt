package com.hedvig.app.feature.referrals.editcode

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CodeTooShortValidationTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldNotAllowSubmitWhenCodeFieldIsBlank() = run {
        activityRule.launchActivity(
            ReferralsEditCodeActivity.newInstance(
                context(),
                "TEST123"
            )
        )

        onScreen<ReferralsEditCodeScreen> {
            editLayout {
                edit {
                    replaceText("")
                }
            }
            save { isDisabled() }
            editLayout {
                edit { replaceText("   ") }
            }
            save { isDisabled() }
            editLayout {
                edit { replaceText("EDITEDCODE123") }
            }
            save { isEnabled() }
        }
    }
}
