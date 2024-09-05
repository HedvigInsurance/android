package com.hedvig.android.notification.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.hedvig.android.logger.logcat

@SuppressLint("MissingPermission")
fun sendHedvigNotification(
  context: Context,
  notificationId: Int,
  notification: Notification,
  notificationChannel: HedvigNotificationChannel,
  notificationSenderName: String?,
) {
  logcat { "$notificationSenderName is going to send a notification" }
  if (
    ActivityCompat.checkSelfPermission(
      context,
      Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
  ) {
    notificationChannel.createChannel(context)
    NotificationManagerCompat
      .from(context)
      .notify(notificationSenderName, notificationId, notification)
  } else {
    logcat { "$notificationSenderName did not have notification permission" }
  }
}
