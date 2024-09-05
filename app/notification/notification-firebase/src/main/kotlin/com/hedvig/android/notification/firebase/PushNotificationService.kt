package com.hedvig.android.notification.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.notification.core.NotificationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject

class PushNotificationService : FirebaseMessagingService() {
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private val notificationSenders by lazy { getKoin().getAll<NotificationSender>().toSet<NotificationSender>() }
  private val fcmTokenManager: FCMTokenManager by inject()

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
    notificationSenders
      .firstOrNull { notificationSender ->
        notificationSender.handlesNotificationType(type)
      }
      ?.run {
        coroutineScope.launch {
          sendNotification(type, remoteMessage)
        }
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
