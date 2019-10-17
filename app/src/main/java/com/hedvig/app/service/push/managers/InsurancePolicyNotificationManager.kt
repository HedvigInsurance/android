package com.hedvig.app.service.push.managers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.service.push.setupNotificationChannel

object InsurancePolicyNotificationManager {
    fun sendInsurancePolicyUpdatedNotification(context: Context) {
        createChannel(context)

        // TODO: This notification should arguably have two actions: `Check PDF` and `Dismiss`
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        LoggedInActivity::class.java
                    )
                )
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val notification = NotificationCompat
            .Builder(
                context,
                INSURANCE_POLICY_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_symbol_android)
            .setContentTitle("TODO Copy")
            .setContentText("TODO Copy")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(INSURANCE_POLICY_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(INSURANCE_POLICY_UPDATED_NOTIFICATION_ID, notification)
    }

    fun sendInsuranceRenewedNotification(context: Context) {
        createChannel(context)

        // TODO: This notification should have a `Open PDF` and a `Dismiss`-action
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        LoggedInActivity::class.java
                    )
                )
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val notification = NotificationCompat
            .Builder(
                context,
                INSURANCE_POLICY_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_symbol_android)
            .setContentTitle("TODO Copy")
            .setContentText("TODO Copy")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(INSURANCE_POLICY_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(INSURANCE_RENEWED_NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            INSURANCE_POLICY_CHANNEL_ID,
            "TODO Copy",
            "TODO Copy"
        )
    }

    private const val INSURANCE_POLICY_CHANNEL_ID = "hedvig-insurance-policy"
    private const val INSURANCE_POLICY_UPDATED_NOTIFICATION_ID = 6
    private const val INSURANCE_RENEWED_NOTIFICATION_ID = 7
}
