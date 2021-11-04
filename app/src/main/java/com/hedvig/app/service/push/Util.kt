package com.hedvig.app.service.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.hedvig.app.util.whenApiVersion

fun setupNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String,
    channelDescription: String? = null
) =
    whenApiVersion(Build.VERSION_CODES.O) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                channelDescription?.let { description = it }
            }
        )
    }

fun getImmutablePendingIntentFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

fun getMutablePendingIntentFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}

const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"
