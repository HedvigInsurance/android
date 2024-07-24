package com.hedvig.android.notification.core

import com.google.firebase.messaging.RemoteMessage

interface NotificationSender {
  fun createChannel()

  suspend fun sendNotification(type: String, remoteMessage: RemoteMessage)

  fun handlesNotificationType(notificationType: String): Boolean
}
