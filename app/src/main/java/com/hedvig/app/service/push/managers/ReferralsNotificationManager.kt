package com.hedvig.app.service.push.managers

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedActivity
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel

object ReferralsNotificationManager {
    fun sendReferralNotification(context: Context, remoteMessage: RemoteMessage) {
        createChannel(context)

        val pendingIntent: PendingIntent? = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    LoggedInActivity.newInstance(
                        context,
                        initialTab = LoggedInTabs.REFERRALS
                    )
                )
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }

        val referralName = remoteMessage.data[DATA_MESSAGE_REFERRED_SUCCESS_NAME]

        val contentText = referralName?.let {
            context.resources.getString(
                R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT_WITH_NAME,
                it
            )
        } ?: context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT)

        val notificationBuilder = createNotificationBuilder(
            context = context,
            title = context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE),
            body = contentText,
            pendingIntent = pendingIntent
        )

        NotificationManagerCompat
            .from(context)
            .notify(REFERRAL_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun sendReferralsEnabledNotification(context: Context) {
        val pendingIntent: PendingIntent? = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    LoggedInActivity.newInstance(
                        context,
                        initialTab = LoggedInTabs.REFERRALS
                    )
                )
                addNextIntentWithParentStack(ReferralsActivatedActivity.newInstance(context))
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }

        val notificationBuilder = createNotificationBuilder(
            context = context,
            title = context.getString(R.string.referrals_notification_feature_added_title),
            body = context.getString(R.string.referrals_notification_feature_added_description),
            pendingIntent = pendingIntent
        )

        notificationBuilder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.referrals_notification_feature_added_description))
        )

        NotificationManagerCompat
            .from(context)
            .notify(REFERRALS_ENABLED_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun sendReferralCampaignNotification(
        context: Context,
        remoteMessage: RemoteMessage
    ) {
        val pendingIntent: PendingIntent? = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    LoggedInActivity.newInstance(
                        context,
                        initialTab = LoggedInTabs.REFERRALS
                    )
                )
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }

        val title = remoteMessage.data[DATA_MESSAGE_CAMPAIGN_TITLE]
        val message = remoteMessage.data[DATA_MESSAGE_CAMPAIGN_MESSAGE]

        val notificationBuilder = createNotificationBuilder(
            context = context,
            title = title,
            body = message,
            pendingIntent = pendingIntent
        )

        NotificationManagerCompat
            .from(context)
            .notify(REFERRALS_ENABLED_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationBuilder(
        context: Context,
        title: String?,
        body: String?,
        pendingIntent: PendingIntent?
    ): NotificationCompat.Builder {
        return NotificationCompat
            .Builder(context, REFERRAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setContentText(title)
            .setContentText(body)
            .setContentTitle(context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setChannelId(REFERRAL_CHANNEL_ID)
            .setContentIntent(pendingIntent)
    }

    fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            REFERRAL_CHANNEL_ID,
            context.resources.getString(R.string.NOTIFICATION_REFERRAL_CHANNEL_NAME),
            context.resources.getString(R.string.NOTIFICATION_REFERRAL_CHANNEL_DESCRIPTION)
        )
    }

    private const val REFERRAL_CHANNEL_ID = "hedvig-referral"
    private const val REFERRAL_NOTIFICATION_ID = 2

    private const val REFERRALS_ENABLED_NOTIFICATION_ID = 8

    internal const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"
    internal const val DATA_MESSAGE_CAMPAIGN_TITLE = "DATA_MESSAGE_CAMPAIGN_TITLE"
    internal const val DATA_MESSAGE_CAMPAIGN_MESSAGE = "DATA_MESSAGE_CAMPAIGN_MESSAGE"
}
