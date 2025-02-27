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
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R
import java.util.concurrent.atomic.AtomicInteger

class GenericNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  private val id = AtomicInteger(100)

  override fun handlesNotificationType(notificationType: String) =
    notificationType == NOTIFICATION_TYPE_GENERIC_COMMUNICATION

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val title = remoteMessage.data.titleFromCustomerIoData() ?: remoteMessage.data[DATA_MESSAGE_TITLE]
    val body = remoteMessage.data.bodyFromCustomerIoData() ?: remoteMessage.data[DATA_MESSAGE_BODY]
    val pendingIntent = PendingIntentCompat.getActivity(
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
      .setContentIntent(pendingIntent)
      .build()

    sendHedvigNotification(
      context = context,
      notificationId = id.getAndIncrement(),
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "GenericNotificationSender",
    )
  }

  companion object {
    private const val NOTIFICATION_TYPE_GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"
  }
}
