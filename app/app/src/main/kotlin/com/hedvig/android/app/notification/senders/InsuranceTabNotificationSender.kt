package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

internal class InsuranceTabNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val deepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override fun handlesNotificationType(notificationType: String): Boolean =
    NOTIFICATION_TYPE_OPEN_INSURANCE_TAB == notificationType

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val title = remoteMessage.data.titleFromCustomerIoData()
    val body = remoteMessage.data.bodyFromCustomerIoData()
    if (title == null || body == null) {
      logcat(LogPriority.ERROR) {
        "InsuranceTabNotificationSender got no title or body, bailing! data:[${remoteMessage.data}]"
      }
      return
    }
    val pendingIntent = PendingIntentCompat.getActivity(
      context,
      RequestCode,
      buildConstants.intentForNotification(deepLinkContainer.insurances.first().toUri()),
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )

    val builder: NotificationCompat.Builder = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    sendHedvigNotification(
      context = context,
      notificationId = NotificationId,
      notification = builder.build(),
      notificationChannel = notificationChannel,
      notificationSenderName = "InsuranceTabNotificationSender",
    )
  }

  companion object {
    private const val NOTIFICATION_TYPE_OPEN_INSURANCE_TAB = "open_insurance_tab"

    // Unique number compared to the other request codes. Has no significance otherwise.
    private const val RequestCode = 9986132

    // Unique number compared to the other notification IDs. Has no significance otherwise.
    private const val NotificationId = 9986133
  }
}
