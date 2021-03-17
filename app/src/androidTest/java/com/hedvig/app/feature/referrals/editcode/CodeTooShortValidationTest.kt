package com.hedvig.app.feature.referrals.editcode

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CodeTooShortValidationTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

    @Test
    fun shouldNotAllowSubmitWhenCodeFieldIsBlank() = run {
        activityRule.launch(
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
