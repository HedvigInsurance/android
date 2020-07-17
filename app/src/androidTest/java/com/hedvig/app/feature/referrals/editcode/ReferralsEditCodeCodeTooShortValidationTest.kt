package com.hedvig.app.feature.referrals.editcode

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralsEditCodeCodeTooShortValidationTest {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsEditCodeActivity::class.java, false, false)

    @Test
    fun shouldNotAllowSubmitWhenCodeFieldIsBlank() {
        activityRule.launchActivity(
            ReferralsEditCodeActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
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
