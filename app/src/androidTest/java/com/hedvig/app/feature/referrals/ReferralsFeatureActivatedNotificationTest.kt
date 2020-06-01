package com.hedvig.app.feature.referrals

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralsFeatureActivatedNotificationTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Test
    fun shouldOpenLoggedInScreenWithReferralsShownWhenOpeningReferralsFeatureActivatedNotification() {
    }

    companion object {
        private val INTENT_WITH_SHOW_REFERRALS = Intent
    }
}
