package com.hedvig.android.app.notification.senders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.MainActivity
import com.hedvig.android.app.notification.DATA_MESSAGE_BODY
import com.hedvig.android.app.notification.DATA_MESSAGE_TITLE
import com.hedvig.android.app.notification.getImmutablePendingIntentFlags
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

class ReferralsNotificationSender(
  private val context: Context,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    when (type) {
      NOTIFICATION_TYPE_REFERRAL_SUCCESS -> sendReferralSuccessfulNotification(remoteMessage)
      NOTIFICATION_TYPE_REFERRALS_CAMPAIGN -> sendReferralCampaignNotification(remoteMessage)
    }
  }

  override fun handlesNotificationType(notificationType: String) = when (notificationType) {
    NOTIFICATION_TYPE_REFERRAL_SUCCESS,
    NOTIFICATION_TYPE_REFERRALS_CAMPAIGN,
    -> true
    else -> false
  }

  private fun sendReferralSuccessfulNotification(remoteMessage: RemoteMessage) {
    val foreverIntent = Intent(Intent.ACTION_VIEW, hedvigDeepLinkContainer.forever.first().toUri())
    val pendingIntent = PendingIntentCompat.getBroadcast(
      context,
      0,
      foreverIntent,
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )

    val referralName = remoteMessage.data[DATA_MESSAGE_REFERRED_SUCCESS_NAME]

    val contentText = referralName?.let {
      context.resources.getString(
        R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT_WITH_NAME,
        it,
      )
    } ?: context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT)

    val notificationBuilder = createNotificationBuilder(
      context = context,
      title = context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE),
      body = contentText,
      pendingIntent = pendingIntent,
    )

    sendNotificationInner(REFERRAL_NOTIFICATION_ID, notificationBuilder.build())
  }

  private fun sendReferralCampaignNotification(remoteMessage: RemoteMessage) {
    val pendingIntent: PendingIntent? = TaskStackBuilder
      .create(context)
      .addNextIntent(
        // todo consider opening Forever directly instead here
        MainActivity.newInstance(context),
      )
      .getPendingIntent(0, getImmutablePendingIntentFlags())

    val title = remoteMessage.data[DATA_MESSAGE_TITLE]
    val message = remoteMessage.data[DATA_MESSAGE_BODY]

    val notificationBuilder = createNotificationBuilder(
      context = context,
      title = title,
      body = message,
      pendingIntent = pendingIntent,
    )

    sendNotificationInner(REFERRALS_CAMPAIGN_ID, notificationBuilder.build())
  }

  private fun sendNotificationInner(id: Int, notification: Notification) {
    sendHedvigNotification(
      context = context,
      notificationId = id,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "ReferralsNotificationSender",
    )
  }

  private fun createNotificationBuilder(
    context: Context,
    title: String?,
    body: String?,
    pendingIntent: PendingIntent?,
  ) = NotificationCompat
    .Builder(context, notificationChannel.channelId)
    .setSmallIcon(R.drawable.ic_hedvig_h)
    .setContentText(title)
    .setContentText(body)
    .setContentTitle(context.resources.getString(R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE))
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setAutoCancel(true)
    .setContentIntent(pendingIntent)

  companion object {
    private const val REFERRAL_NOTIFICATION_ID = 2

    private const val REFERRALS_CAMPAIGN_ID = 12

    internal const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"

    private const val NOTIFICATION_TYPE_REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
    private const val NOTIFICATION_TYPE_REFERRALS_CAMPAIGN = "REFERRALS_CAMPAIGN"
  }
}
