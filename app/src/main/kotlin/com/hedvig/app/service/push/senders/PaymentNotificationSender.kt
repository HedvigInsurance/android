package com.hedvig.app.service.push.senders

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.core.common.android.notification.setupNotificationChannel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.tracking.NotificationOpenedTrackingActivity
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentNotificationSender(
  private val context: Context,
  private val marketManager: MarketManager,
  private val featureManager: FeatureManager,
) : NotificationSender {
  override fun createChannel() {
    setupNotificationChannel(
      context,
      PAYMENTS_CHANNEL_ID,
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_TITLE),
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHANNEL_PAYMENT_DESCRIPTION),
    )
  }

  override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    when (type) {
      NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT -> sendConnectDirectDebitNotification()
      NOTIFICATION_TYPE_PAYMENT_FAILED -> sendPaymentFailedNotification()
    }
  }

  private fun sendConnectDirectDebitNotification() {
    val market = marketManager.market ?: return
    CoroutineScope(Dispatchers.IO).launch {
      val pendingIntent = TaskStackBuilder
        .create(context)
        .run {
          addNextIntentWithParentStack(
            Intent(
              context,
              LoggedInActivity::class.java,
            ),
          )
          try {
            addNextIntentWithParentStack(
              connectPayinIntent(
                context,
                featureManager.getPaymentType(),
                market,
                false,
              ),
            )
          } catch (error: IllegalArgumentException) {
            e {
              "Illegal market and payment type, could not create payin intent. " +
                "Market: $market, PaymentType: ${featureManager.getPaymentType()}"
            }
          }

          addNextIntentWithParentStack(
            NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT),
          )
          getPendingIntent(0, getImmutablePendingIntentFlags())
        }

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

      NotificationManagerCompat
        .from(context)
        .notify(CONNECT_DIRECT_DEBIT_NOTIFICATION_ID, notification)
    }
  }

  private fun sendPaymentFailedNotification() {
    val pendingIntent = TaskStackBuilder
      .create(context)
      .run {
        addNextIntentWithParentStack(
          Intent(
            context,
            LoggedInActivity::class.java,
          ),
        )
        addNextIntentWithParentStack(
          Intent(
            context,
            PaymentActivity::class.java,
          ),
        )
        addNextIntentWithParentStack(
          NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_PAYMENT_FAILED),
        )
        getPendingIntent(0, getImmutablePendingIntentFlags())
      }

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

    NotificationManagerCompat
      .from(context)
      .notify(PAYMENT_FAILED_NOTIFICATION_ID, notification)
  }

  override fun handlesNotificationType(notificationType: String) = when (notificationType) {
    NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT,
    NOTIFICATION_TYPE_PAYMENT_FAILED,
    -> true
    else -> false
  }

  companion object {
    private const val PAYMENTS_CHANNEL_ID = "hedvig-payments"
    private const val CONNECT_DIRECT_DEBIT_NOTIFICATION_ID = 3
    private const val PAYMENT_FAILED_NOTIFICATION_ID = 5
    private const val NOTIFICATION_TYPE_CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
    private const val NOTIFICATION_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED"
  }
}
