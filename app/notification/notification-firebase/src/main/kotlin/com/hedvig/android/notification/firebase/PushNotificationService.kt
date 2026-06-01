package com.hedvig.android.notification.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.notification.core.NotificationSender
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  @Inject private lateinit var notificationSenders: Set<NotificationSender>
  @Inject private lateinit var fcmTokenManager: FCMTokenManager

  override fun onCreate() {
    super.onCreate()
    (applicationContext as PushNotificationGraphProvider).inject(this)
  }

  override fun onNewToken(token: String) {
    logcat(LogPriority.INFO) { "FCM onNewToken:$token" }
    coroutineScope.launch {
      fcmTokenManager.saveLocallyAndUploadTokenToBackend(token)
    }
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    logcat(LogPriority.INFO) { "FCM onMessageReceived, with data:${remoteMessage.data}" }
    val type = remoteMessage.data[NOTIFICATION_TYPE_KEY]
    if (type == null) {
      logcat(LogPriority.ERROR) { "FCM onMessageReceived, type was not present. Data:${remoteMessage.data}" }
      return
    }
    logcat(LogPriority.INFO) { "FCM onMessageReceived, type:$type" }
    val matchingNotificationSender = notificationSenders.firstOrNull { notificationSender ->
      notificationSender.handlesNotificationType(type)
    }
    if (matchingNotificationSender != null) {
      coroutineScope.launch {
        matchingNotificationSender.sendNotification(type, remoteMessage)
      }
    } else {
      logcat(LogPriority.ERROR) { "FCM onMessageReceived, no matching notification sender for type:$type" }
    }
  }

  override fun onDestroy() {
    coroutineScope.cancel()
    super.onDestroy()
  }

  companion object {
    private const val NOTIFICATION_TYPE_KEY = "TYPE"
  }
}
