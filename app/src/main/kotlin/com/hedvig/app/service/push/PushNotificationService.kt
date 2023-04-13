package com.hedvig.app.service.push

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.util.extensions.injectAll
import org.koin.android.ext.android.inject

class PushNotificationService : FirebaseMessagingService() {

  private val notificationSenders by injectAll<NotificationSender>()
  private val sharedPreferences: SharedPreferences by inject()

  override fun onCreate() {
    super.onCreate()

    notificationSenders.forEach { it.createChannel() }
  }

  override fun onNewToken(token: String) {
    sharedPreferences.edit().putString("notification_token", token).apply()
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val type = remoteMessage.data[NOTIFICATION_TYPE_KEY] ?: return
    notificationSenders.forEach { sender ->
      if (sender.handlesNotificationType(type)) {
        sender.sendNotification(type, remoteMessage)
        return@onMessageReceived
      }
    }
  }

  companion object {
    private const val NOTIFICATION_TYPE_KEY = "TYPE"
  }
}
