package com.hedvig.android.core.common.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.hedvig.android.core.common.android.whenApiVersion

fun setupNotificationChannel(
  context: Context,
  channelId: String,
  channelName: String,
  channelDescription: String? = null,
) {
  whenApiVersion(Build.VERSION_CODES.O) {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return@whenApiVersion
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
