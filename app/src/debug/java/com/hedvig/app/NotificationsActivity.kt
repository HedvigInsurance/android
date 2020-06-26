package com.hedvig.app

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import com.hedvig.app.service.push.managers.ReferralsNotificationManager.DATA_MESSAGE_REFERRED_SUCCESS_NAME
import kotlinx.android.synthetic.debug.activity_generic_development.*

class NotificationsActivity : BaseActivity(R.layout.activity_generic_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item(
                    "Referrals Enabled"
                ) { ReferralsNotificationManager.sendReferralsEnabledNotification(this) },
                GenericDevelopmentAdapter.Item(
                    "Referrals Success"
                ) {
                    ReferralsNotificationManager.sendReferralNotification(
                        this, RemoteMessage(
                            bundleOf(
                                DATA_MESSAGE_REFERRED_SUCCESS_NAME to "William"
                            )
                        )
                    )
                }
            )
        )
    }
}
