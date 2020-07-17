package com.hedvig.app.feature.referrals.editcode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodeTooLongValidationTest {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldNotAllowSubmitWhenCodeIsTooLongAndShowAnError() {
        activityRule.launchActivity(
            ReferralsEditCodeActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
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
                hasError(
                    ApplicationProvider.getApplicationContext<Context>()
                        .getString(R.string.referrals_change_code_sheet_error_max_length)
                )
                edit { replaceText("ABCDEFGHIJKLMNOPQRSTUVU") }
            }
            save { isEnabled() }
        }
    }
}
