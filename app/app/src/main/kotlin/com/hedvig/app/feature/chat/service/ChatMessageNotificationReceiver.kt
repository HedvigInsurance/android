package com.hedvig.app.feature.chat.service

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hedvig.app.feature.chat.service.ChatNotificationSender.Companion.CHAT_REPLY_DATA_NOTIFICATION_ID
import com.hedvig.app.feature.chat.service.ChatNotificationSender.Companion.CHAT_REPLY_KEY
import java.util.concurrent.TimeUnit

class ChatMessageNotificationReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val replyText: CharSequence = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(CHAT_REPLY_KEY) ?: return
    val notificationId: Int = intent.getIntExtra(CHAT_REPLY_DATA_NOTIFICATION_ID, 0)

    val work = OneTimeWorkRequestBuilder<ReplyWorker>()
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.SECONDS)
      .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
      .setInputData(
        workDataOf(
          REPLY_TEXT to replyText.toString(),
          NOTIFICATION_ID to notificationId,
        ),
      )
      .build()

    WorkManager.getInstance(context).enqueue(work)
  }

  companion object {
    const val REPLY_TEXT = "REPLY_TEXT"
    const val NOTIFICATION_ID = "NOTIFICATION_ID"
  }
}
