package com.hedvig.app.service.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.hedvig.app.util.whenApiVersion

fun setupNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String,
    channelDescription: String?
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
