package com.hedvig.app.feature.loggedin.service

import android.content.Context
import com.hedvig.app.feature.loggedin.ui.TabNotification

class TabNotificationService(
    private val context: Context
) {
    fun getTabNotification(): TabNotification? {
        if (!context
                .getSharedPreferences(TAB_NOTIFICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(HAS_BEEN_NOTIFIED_ABOUT_REFERRALS, false)
        ) {

            return TabNotification.REFERRALS
        }

        return null
    }

    fun hasBeenNotifiedAboutReferrals() {
        context
            .getSharedPreferences(TAB_NOTIFICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(HAS_BEEN_NOTIFIED_ABOUT_REFERRALS, true)
            .apply()
    }

    companion object {
        private const val TAB_NOTIFICATION_SHARED_PREFERENCES = "tab_notifications"
        private const val HAS_BEEN_NOTIFIED_ABOUT_REFERRALS = "has_been_notified_about_referrals"
    }
}
