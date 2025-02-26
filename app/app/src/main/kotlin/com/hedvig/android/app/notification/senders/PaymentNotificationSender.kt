package com.hedvig.android.app.notification.senders

import android.app.Notification
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

class PaymentNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val deepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    when (type) {
      NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT -> sendConnectDirectDebitNotification()
      NOTIFICATION_TYPE_PAYMENT_FAILED -> sendPaymentFailedNotification()
    }
  }

  override fun handlesNotificationType(notificationType: String) = when (notificationType) {
    NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT,
    NOTIFICATION_TYPE_PAYMENT_FAILED,
    -> true
    else -> false
  }

  private fun sendConnectDirectDebitNotification() {
    val pendingIntent = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(deepLinkContainer.connectPayment.first().toUri()),
      PendingIntent.FLAG_UPDATE_CURRENT,
      false,
    )
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setContentTitle(context.getString(R.string.NOTIFICATION_CONNECT_DD_TITLE))
      .setContentText(context.getString(R.string.NOTIFICATION_CONNECT_DD_BODY))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    sendNotificationInner(CONNECT_DIRECT_DEBIT_NOTIFICATION_ID, notification)
  }

  private fun sendPaymentFailedNotification() {
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
      .setContentTitle(context.getString(R.string.NOTIFICATION_PAYMENT_FAILED_TITLE))
      .setContentText(context.getString(R.string.NOTIFICATION_PAYMENT_FAILED_BODY))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    sendNotificationInner(PAYMENT_FAILED_NOTIFICATION_ID, notification)
  }

  private fun sendNotificationInner(id: Int, notification: Notification) {
    sendHedvigNotification(
      context = context,
      notificationId = id,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "PaymentNotificationSender",
    )
  }

  companion object {
    private const val CONNECT_DIRECT_DEBIT_NOTIFICATION_ID = 3
    private const val PAYMENT_FAILED_NOTIFICATION_ID = 5
    private const val NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
    private const val NOTIFICATION_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED"
  }
}
