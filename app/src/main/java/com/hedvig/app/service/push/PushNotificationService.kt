package com.hedvig.app.service.push

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.util.extensions.injectAll
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class PushNotificationService : FirebaseMessagingService() {
  private val marketManager: MarketManager by inject()

  private val notificationSenders by injectAll<NotificationSender>()

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(Language.fromSettings(base, marketManager.market).apply(base))
  }

  override fun onCreate() {
    super.onCreate()

    notificationSenders.forEach { it.createChannel() }
  }

  override fun onNewToken(token: String) {
    val work = OneTimeWorkRequest
      .Builder(PushNotificationWorker::class.java)
      .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
      .setInputData(
        Data.Builder()
          .putString(PushNotificationWorker.PUSH_TOKEN, token)
          .build(),
      )
      .build()
    WorkManager
      .getInstance(this)
      .beginWith(work)
      .enqueue()
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
