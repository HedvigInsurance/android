package com.hedvig.app.service.push

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.service.push.managers.ChatNotificationManager
import com.hedvig.app.service.push.managers.GenericNotificationManager
import com.hedvig.app.service.push.managers.PaymentNotificationManager
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import i
import java.util.concurrent.TimeUnit

class PushNotificationService : FirebaseMessagingService() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(Language.fromSettings(base)?.apply(base))
    }

    override fun onCreate() {
        super.onCreate()

        ChatNotificationManager.createChannel(this)
        ReferralsNotificationManager.createChannel(this)
        PaymentNotificationManager.createChannel(this)
        GenericNotificationManager.createChannel(this)
    }

    override fun onNewToken(token: String) {
        i { "Got new token: $token" }
        val work = OneTimeWorkRequest
            .Builder(PushNotificationWorker::class.java)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
            .setInputData(
                Data.Builder()
                    .putString(PushNotificationWorker.PUSH_TOKEN, token)
                    .build()
            )
            .build()
        WorkManager
            .getInstance(this)
            .beginWith(work)
            .enqueue()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        when (remoteMessage.data[NOTIFICATION_TYPE_KEY]) {
            NOTIFICATION_TYPE_NEW_MESSAGE -> ChatNotificationManager
                .sendChatNotification(this)
            NOTIFICATION_TYPE_REFERRAL_SUCCESS -> ReferralsNotificationManager
                .sendReferralNotification(this, remoteMessage)
            NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT -> PaymentNotificationManager
                .sendDirectDebitNotification(this)
            NOTIFICATION_TYPE_PAYMENT_FAILED -> PaymentNotificationManager
                .sendPaymentFailedNotification(this)
/*
            NOTIFICATION_TYPE_CLAIM_PAID -> PaymentNotificationManager
                .sendClaimPaidNotification(this, remoteMessage)
            NOTIFICATION_TYPE_INSURANCE_POLICY_UPDATED -> InsurancePolicyNotificationManager
                .sendInsurancePolicyUpdatedNotification(this)
            NOTIFICATION_TYPE_INSURANCE_RENEWED -> InsurancePolicyNotificationManager
                .sendInsuranceRenewedNotification(this)
*/
            NOTIFICATION_TYPE_GENERIC_COMMUNICATION -> GenericNotificationManager
                .sendGenericNotification(this, remoteMessage)
            else -> ChatNotificationManager
                .sendDefaultNotification(this, remoteMessage)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "hedvig-push"
        const val NOTIFICATION_ID = 0

        const val NOTIFICATION_TYPE_KEY = "TYPE"
        const val NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE"

        const val NOTIFICATION_TYPE_REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
        const val NOTIFICATION_TYPE_REFERRALS_ENABLED = "REFERRALS_ENABLED"

        const val NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
        const val NOTIFICATION_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED"

        /*
                const val NOTIFICATION_TYPE_CLAIM_PAID = "CLAIM_PAID"
                const val NOTIFICATION_TYPE_INSURANCE_POLICY_UPDATED = "INSURANCE_POLICY_UPDATED"
                const val NOTIFICATION_TYPE_INSURANCE_RENEWED = "INSURANCE_RENEWED"
        */
        const val NOTIFICATION_TYPE_GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"

        const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT =
            "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT"
    }
}
