package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

class TravelAddonSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val deepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val pendingIntent = PendingIntentCompat.getActivity(
      context,
      RequestCode,
      buildConstants.intentForNotification(deepLinkContainer.travelAddon.first().toUri()),
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )
    val title = remoteMessage.titleFromCustomerIoData()
    val body = remoteMessage.bodyFromCustomerIoData()
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
      notificationId = NotificationId,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "TravelAddonSender",
    )
  }

  override fun handlesNotificationType(notificationType: String): Boolean =
    NOTIFICATION_TYPE_OPEN_TRAVEL_ADDON_FLOW == notificationType

  companion object {
    private const val NOTIFICATION_TYPE_OPEN_TRAVEL_ADDON_FLOW = "ADDON_TRAVEL"

    // Unique number compared to the other request codes. Has no significance otherwise.
    private const val RequestCode = 656465

    // Unique number compared to the other notification IDs. Has no significance otherwise.
    private const val NotificationId = 656466
  }
}
