package com.hedvig.app.feature.referrals.editcode

import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
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
