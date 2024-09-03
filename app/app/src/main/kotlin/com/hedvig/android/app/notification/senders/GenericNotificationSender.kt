package com.hedvig.android.app.notification.senders

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.MainActivity
import com.hedvig.android.app.notification.DATA_MESSAGE_BODY
import com.hedvig.android.app.notification.DATA_MESSAGE_TITLE
import com.hedvig.android.app.notification.getImmutablePendingIntentFlags
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R
import java.util.concurrent.atomic.AtomicInteger

class GenericNotificationSender(
  private val context: Context,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  private val id = AtomicInteger(100)

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val title = remoteMessage.data[DATA_MESSAGE_TITLE]
    val body = remoteMessage.data[DATA_MESSAGE_BODY]
    val pendingIntent = TaskStackBuilder
      .create(context)
      .addNextIntent(Intent(context, MainActivity::class.java))
      .getPendingIntent(0, getImmutablePendingIntentFlags())
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
      notificationSender = "GenericNotificationSender",
      notificationId = id.getAndIncrement(),
      notification = notification,
      notificationChannel = notificationChannel,
    )
  }

  override fun handlesNotificationType(notificationType: String) =
    notificationType == NOTIFICATION_TYPE_GENERIC_COMMUNICATION

  companion object {
    const val NOTIFICATION_TYPE_GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"
  }
}
