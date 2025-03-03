package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.DATA_MESSAGE_BODY
import com.hedvig.android.app.notification.DATA_MESSAGE_TITLE
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

class CrossSellNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override fun handlesNotificationType(notificationType: String) = notificationType == NOTIFICATION_CROSS_SELL

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    // TODO drop support for this notification type if we see we never get it
    logcat(LogPriority.ASSERT) { "We never expect to get a notification of type $type" }
    return
    val title = remoteMessage.data[DATA_MESSAGE_TITLE]
    val body = remoteMessage.data[DATA_MESSAGE_BODY]

    @Suppress("UNUSED_VARIABLE") // Unused when there's no cross sale detail screen in the app
    val id = remoteMessage.data[CROSS_SELL_ID]

    // todo: Consider opening some cross sell detail screen here instead
    val intent = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(deepLinkUri = null),
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(intent)
      .build()
    sendHedvigNotification(
      context = context,
      notificationId = CROSS_SELL_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "CrossSellNotificationSender",
    )
  }

  companion object {
    private const val CROSS_SELL_ID = "CROSS_SELL_ID"
    private const val CROSS_SELL_NOTIFICATION_ID = 11

    private const val NOTIFICATION_CROSS_SELL = "CROSS_SELL"
  }
}
