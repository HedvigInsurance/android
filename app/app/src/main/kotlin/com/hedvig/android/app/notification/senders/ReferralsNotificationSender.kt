package com.hedvig.android.app.notification.senders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
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
    sendReferralSuccessfulNotification(remoteMessage)
  }

  override fun handlesNotificationType(notificationType: String) =
    notificationType == NOTIFICATION_TYPE_REFERRAL_SUCCESS

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

    internal const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"

    private const val NOTIFICATION_TYPE_REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
  }
}
