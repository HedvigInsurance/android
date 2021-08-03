package com.hedvig.app.feature.referrals.editcode

import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class CodeTooLongValidationTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(ReferralsEditCodeActivity::class.java)

    @Test
    fun shouldNotAllowSubmitWhenCodeIsTooLongAndShowAnError() = run {
        activityRule.launch(
            ReferralsEditCodeActivity.newInstance(
                context(),
                "TEST123"
            )
        )

        onScreen<ReferralsEditCodeScreen> {
            editLayout {
                edit {
                    replaceText("ABCDEFGHIJKLMNOPQRSTUVUXYZ")
                }
            }
            save { isDisabled() }
            editLayout {
                isErrorEnabled()
                hasError(R.string.referrals_change_code_sheet_error_max_length)
                edit { replaceText("ABCDEFGHIJKLMNOPQRSTUVU") }
            }
            save { isEnabled() }
        }
    }
}
