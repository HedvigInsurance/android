package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R.drawable.ic_hedvig_h
import hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_CONTENT
import hedvig.resources.R.string.NOTIFICATION_REFERRAL_COMPLETED_TITLE

class ReferralsNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val deepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override fun handlesNotificationType(notificationType: String) =
    notificationType == NOTIFICATION_TYPE_REFERRAL_SUCCESS

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val pendingIntent = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(deepLinkContainer.forever.first().toUri()),
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )
    val title = remoteMessage.titleFromCustomerIoData()
      ?: context.resources.getString(NOTIFICATION_REFERRAL_COMPLETED_TITLE)
    val body = remoteMessage.bodyFromCustomerIoData()
      ?: context.resources.getString(NOTIFICATION_REFERRAL_COMPLETED_CONTENT)
    val notificationBuilder = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
    sendHedvigNotification(
      context = context,
      notificationId = REFERRAL_NOTIFICATION_ID,
      notification = notificationBuilder.build(),
      notificationChannel = notificationChannel,
      notificationSenderName = "ReferralsNotificationSender",
    )
  }

  companion object {
    private const val REFERRAL_NOTIFICATION_ID = 2

    private const val NOTIFICATION_TYPE_REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
  }
}
