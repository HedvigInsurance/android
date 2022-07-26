package com.hedvig.app.service.push.senders

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.tracking.NotificationOpenedTrackingActivity
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel

class ReferralsNotificationSender(
  private val context: Context,
) : NotificationSender {
  override fun createChannel() {
    setupNotificationChannel(
      context,
      REFERRAL_CHANNEL_ID,
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_REFERRAL_CHANNEL_NAME),
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_REFERRAL_CHANNEL_DESCRIPTION),
    )
  }

  override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    when (type) {
      NOTIFICATION_TYPE_REFERRAL_SUCCESS -> sendReferralSuccessfulNotification(remoteMessage)
      NOTIFICATION_TYPE_REFERRALS_CAMPAIGN -> sendReferralCampaignNotification(remoteMessage)
    }
  }

  internal fun sendReferralSuccessfulNotification(remoteMessage: RemoteMessage) {
    val pendingIntent: PendingIntent? = TaskStackBuilder
      .create(context)
      .run {
        addNextIntentWithParentStack(
          LoggedInActivity.newInstance(
            context,
            initialTab = LoggedInTabs.REFERRALS,
          ),
        )
        addNextIntentWithParentStack(
          NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_REFERRAL_SUCCESS),
        )
        getPendingIntent(0, getImmutablePendingIntentFlags())
      }

    val referralName = remoteMessage.data[DATA_MESSAGE_REFERRED_SUCCESS_NAME]

    val contentText = referralName?.let {
      context.resources.getString(
        hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT_WITH_NAME,
        it,
      )
    } ?: context.resources.getString(hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT)

    val notificationBuilder = createNotificationBuilder(
      context = context,
      title = context.resources.getString(hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE),
      body = contentText,
      pendingIntent = pendingIntent,
    )

    NotificationManagerCompat
      .from(context)
      .notify(REFERRAL_NOTIFICATION_ID, notificationBuilder.build())
  }

  internal fun sendReferralCampaignNotification(remoteMessage: RemoteMessage) {
    val pendingIntent: PendingIntent? = TaskStackBuilder
      .create(context)
      .run {
        addNextIntentWithParentStack(
          LoggedInActivity.newInstance(
            context,
            initialTab = LoggedInTabs.REFERRALS,
          ),
        )
        addNextIntentWithParentStack(
          NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_REFERRALS_CAMPAIGN),
        )
        getPendingIntent(0, getImmutablePendingIntentFlags())
      }

    val title = remoteMessage.data[DATA_MESSAGE_TITLE]
    val message = remoteMessage.data[DATA_MESSAGE_BODY]

    val notificationBuilder = createNotificationBuilder(
      context = context,
      title = title,
      body = message,
      pendingIntent = pendingIntent,
    )

    NotificationManagerCompat
      .from(context)
      .notify(REFERRALS_CAMPAIGN_ID, notificationBuilder.build())
  }

  override fun handlesNotificationType(notificationType: String) = when (notificationType) {
    NOTIFICATION_TYPE_REFERRAL_SUCCESS,
    NOTIFICATION_TYPE_REFERRALS_CAMPAIGN,
    -> true
    else -> false
  }

  private fun createNotificationBuilder(
    context: Context,
    title: String?,
    body: String?,
    pendingIntent: PendingIntent?,
  ) = NotificationCompat
    .Builder(context, REFERRAL_CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_hedvig_h)
    .setContentText(title)
    .setContentText(body)
    .setContentTitle(context.resources.getString(hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE))
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setAutoCancel(true)
    .setChannelId(REFERRAL_CHANNEL_ID)
    .setContentIntent(pendingIntent)

  companion object {
    private const val REFERRAL_CHANNEL_ID = "hedvig-referral"
    private const val REFERRAL_NOTIFICATION_ID = 2

    private const val REFERRALS_CAMPAIGN_ID = 12

    internal const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"

    private const val NOTIFICATION_TYPE_REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
    private const val NOTIFICATION_TYPE_REFERRALS_CAMPAIGN = "REFERRALS_CAMPAIGN"
  }
}
