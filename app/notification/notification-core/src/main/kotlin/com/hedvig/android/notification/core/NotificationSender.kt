package com.hedvig.android.notification.core

import com.google.firebase.messaging.RemoteMessage

interface NotificationSender {
  suspend fun sendNotification(type: String, remoteMessage: RemoteMessage)

  fun handlesNotificationType(notificationType: String): Boolean
}
