package com.hedvig.android.app.notification.senders

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.MainActivity
import com.hedvig.android.app.notification.getImmutablePendingIntentFlags
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.android.notification.setupNotificationChannel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentNotificationSender(
  private val context: Context,
  private val applicationScope: ApplicationScope,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) : NotificationSender {
  override fun createChannel() {
    setupNotificationChannel(
      context,
      PAYMENTS_CHANNEL_ID,
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_TITLE),
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_DESCRIPTION),
    )
  }

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
        .Builder(
          context,
          PAYMENTS_CHANNEL_ID,
        )
        .setSmallIcon(hedvig.resources.R.drawable.ic_hedvig_h)
        .setContentTitle(context.getString(hedvig.resources.R.string.NOTIFICATION_CONNECT_DD_TITLE))
        .setContentText(context.getString(hedvig.resources.R.string.NOTIFICATION_CONNECT_DD_BODY))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .setChannelId(PAYMENTS_CHANNEL_ID)
        .setContentIntent(pendingIntent)
        .build()

      sendNotificationInner(CONNECT_DIRECT_DEBIT_NOTIFICATION_ID, notification)
    }
  }

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
      .Builder(
        context,
        PAYMENTS_CHANNEL_ID,
      )
      .setSmallIcon(hedvig.resources.R.drawable.ic_hedvig_h)
      .setContentTitle(context.getString(hedvig.resources.R.string.NOTIFICATION_PAYMENT_FAILED_TITLE))
      .setContentText(context.getString(hedvig.resources.R.string.NOTIFICATION_PAYMENT_FAILED_BODY))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
      .setChannelId(PAYMENTS_CHANNEL_ID)
      .setContentIntent(pendingIntent)
      .build()

    sendNotificationInner(PAYMENT_FAILED_NOTIFICATION_ID, notification)
  }

  private fun sendNotificationInner(id: Int, notification: Notification) {
    sendHedvigNotification(
      context = context,
      notificationSender = "PaymentNotificationSender",
      notificationId = id,
      notification = notification,
    )
  }

  companion object {
    private const val PAYMENTS_CHANNEL_ID = "hedvig-payments"
    private const val CONNECT_DIRECT_DEBIT_NOTIFICATION_ID = 3
    private const val PAYMENT_FAILED_NOTIFICATION_ID = 5
    private const val NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
    private const val NOTIFICATION_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED"
  }
}
