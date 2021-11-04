package com.hedvig.app.service.push.managers

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel

class PaymentNotificationManager(
    private val marketManager: MarketManager
) {
    fun sendDirectDebitNotification(context: Context) {
        val market = marketManager.market ?: return
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        LoggedInActivity::class.java
                    )
                )
                addNextIntentWithParentStack(
                    market.connectPayin(context)
                )
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }

        val notification = NotificationCompat
            .Builder(
                context,
                PAYMENTS_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setContentTitle(context.getString(R.string.NOTIFICATION_CONNECT_DD_TITLE))
            .setContentText(context.getString(R.string.NOTIFICATION_CONNECT_DD_BODY))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(PAYMENTS_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(CONNECT_DIRECT_DEBIT_NOTIFICATION_ID, notification)
    }

    fun sendPaymentFailedNotification(context: Context) {
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        LoggedInActivity::class.java
                    )
                )
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        PaymentActivity::class.java
                    )
                )
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }

        val notification = NotificationCompat
            .Builder(
                context,
                PAYMENTS_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setContentTitle(context.getString(R.string.NOTIFICATION_PAYMENT_FAILED_TITLE))
            .setContentText(context.getString(R.string.NOTIFICATION_PAYMENT_FAILED_BODY))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(PAYMENTS_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(PAYMENT_FAILED_NOTIFICATION_ID, notification)
    }

    fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            PAYMENTS_CHANNEL_ID,
            context.resources.getString(R.string.NOTIFICATION_CHANNEL_PAYMENT_TITLE),
            context.resources.getString(R.string.NOTIFICATION_CHANNEL_PAYMENT_DESCRIPTION)
        )
    }

    companion object {
        private const val PAYMENTS_CHANNEL_ID = "hedvig-payments"
        private const val CONNECT_DIRECT_DEBIT_NOTIFICATION_ID = 3
        private const val PAYMENT_FAILED_NOTIFICATION_ID = 5
    }
}
