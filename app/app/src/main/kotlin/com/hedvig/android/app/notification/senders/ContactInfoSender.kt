package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.getImmutablePendingIntentFlags
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

class ContactInfoSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val deepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val contactInfoIntent = Intent().apply {
      action = Intent.ACTION_VIEW
      data = Uri.parse(deepLinkContainer.contactInfo.first())
      component = ComponentName(buildConstants.appId, MainActivityFullyQualifiedName)
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      contactInfoIntent,
      getImmutablePendingIntentFlags(),
    )
    val title = remoteMessage.data.titleFromCustomerIoData()
    val body = remoteMessage.data.bodyFromCustomerIoData()
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
      notificationId = ContactInfoNotificationId,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "ContactInfoSender",
    )
  }

  override fun handlesNotificationType(notificationType: String): Boolean {
    return notificationType == NOTIFICATION_TYPE_CONTACT_INFO
  }

  companion object {
    private const val NOTIFICATION_TYPE_CONTACT_INFO = "OPEN_CONTACT_INFO"

    // Randomly chosen, holds no specific importance
    private const val ContactInfoNotificationId = 100
  }
}
