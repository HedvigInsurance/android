package com.hedvig.app.feature.chat.service

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.hedvig.app.feature.chat.service.ChatNotificationManager.CHAT_REPLY_DATA_NOTIFICATION_ID
import com.hedvig.app.feature.chat.service.ChatNotificationManager.CHAT_REPLY_KEY
import java.util.concurrent.TimeUnit

class ChatMessageNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val replyText =
            RemoteInput.getResultsFromIntent(intent)?.getCharSequence(CHAT_REPLY_KEY) ?: return
        val notificationId = intent.getIntExtra(CHAT_REPLY_DATA_NOTIFICATION_ID, 0)

        val work = OneTimeWorkRequest.Builder(ReplyWorker::class.java)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
            .setInputData(
                Data
                    .Builder()
                    .putString(REPLY_TEXT, replyText.toString())
                    .putInt(NOTIFICATION_ID, notificationId)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(context)
            .beginWith(work)
            .enqueue()
    }

    companion object {
        const val REPLY_TEXT = "REPLY_TEXT"
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
    }
}
