package com.hedvig.android.core.common.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

fun setupNotificationChannel(
  context: Context,
  channelId: String,
  channelName: String,
  channelDescription: String? = null,
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return
    notificationManager.createNotificationChannel(
      NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        channelDescription?.let { description = it }
      },
    )
  }
}
