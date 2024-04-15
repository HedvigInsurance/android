package com.hedvig.app.service.push.senders

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.MainActivity
import com.hedvig.android.core.common.android.notification.setupNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import java.util.concurrent.atomic.AtomicInteger

class GenericNotificationSender(
  private val context: Context,
) : NotificationSender {
  private val id = AtomicInteger(100)

  override fun createChannel() {
    setupNotificationChannel(
      context,
      GENERIC_CHANNEL_ID,
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_GENERIC_TITLE),
    )
  }

  override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val title = remoteMessage.data[DATA_MESSAGE_TITLE]
    val body = remoteMessage.data[DATA_MESSAGE_BODY]
    val pendingIntent = TaskStackBuilder
      .create(context)
      .addNextIntent(Intent(context, MainActivity::class.java))
      .getPendingIntent(0, getImmutablePendingIntentFlags())
    val notification = NotificationCompat
      .Builder(
        context,
        GENERIC_CHANNEL_ID,
      )
      .setSmallIcon(hedvig.resources.R.drawable.ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setChannelId(GENERIC_CHANNEL_ID)
      .setContentIntent(pendingIntent)
      .build()

    sendHedvigNotification(
      context = context,
      notificationSender = "GenericNotificationSender",
      notificationId = id.getAndIncrement(),
      notification = notification,
    )
  }

  override fun handlesNotificationType(notificationType: String) =
    notificationType == NOTIFICATION_TYPE_GENERIC_COMMUNICATION

  companion object {
    const val NOTIFICATION_TYPE_GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"

    private const val GENERIC_CHANNEL_ID = "hedvig-generic"
  }
}
