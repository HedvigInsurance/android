package com.hedvig.android.app.notification.senders

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.media3.common.util.UnstableApi
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.MainActivity
import com.hedvig.android.app.notification.getImmutablePendingIntentFlags
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentNotificationSender(
  private val context: Context,
  private val applicationScope: ApplicationScope,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  @OptIn(UnstableApi::class)
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
    applicationScope.launch(Dispatchers.IO) {
      val connectPaymentDeepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(hedvigDeepLinkContainer.connectPayment))
      val pendingIntent = TaskStackBuilder
        .create(context)
        .addNextIntent(connectPaymentDeepLinkIntent)
        .getPendingIntent(0, getImmutablePendingIntentFlags())

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
  }

  @androidx.media3.common.util.UnstableApi
  @OptIn(UnstableApi::class)
  private fun sendPaymentFailedNotification() {
    val pendingIntent = TaskStackBuilder
      .create(context)
      .addNextIntent(
        Intent(
          context,
          MainActivity::class.java,
        ),
      )
      .getPendingIntent(0, getImmutablePendingIntentFlags())

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
