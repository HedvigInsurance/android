package com.hedvig.app.service.push

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.util.extensions.injectAll
import java.util.concurrent.TimeUnit

class PushNotificationService : FirebaseMessagingService() {

  private val notificationSenders by injectAll<NotificationSender>()

  override fun onCreate() {
    super.onCreate()

    notificationSenders.forEach { it.createChannel() }
  }

  override fun onNewToken(token: String) {
    val work = OneTimeWorkRequest
      .Builder(PushNotificationWorker::class.java)
      .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
      .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
      .setInputData(
        Data.Builder()
          .putString(PushNotificationWorker.PUSH_TOKEN, token)
          .build(),
      )
      .build()
    WorkManager.getInstance(this).enqueue(work)
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
