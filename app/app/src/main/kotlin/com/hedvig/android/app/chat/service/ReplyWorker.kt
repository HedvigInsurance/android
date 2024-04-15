package com.hedvig.android.app.chat.service

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.Either
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

class ReplyWorker(
  private val context: Context,
  params: WorkerParameters,
  private val chatRepository: ChatRepository,
  private val chatNotificationSender: ChatNotificationSender,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val replyText = inputData.getString(ChatMessageNotificationReceiver.REPLY_TEXT) ?: return Result.failure()
    val sendChatMessageResponse = chatRepository.sendMessage(
      text = replyText,
      context = null,
    )

    when (sendChatMessageResponse) {
      is Either.Left -> {
        val error = sendChatMessageResponse.value
        logcat(LogPriority.WARN, error.throwable) {
          buildString {
            append("Chat: Replying through ReplyWorker failed")
            if (error.message != null) {
              append(". Message:${error.message}")
            }
          }
        }
        return Result.retry()
      }
      is Either.Right -> {
        val notificationId = inputData.getInt(ChatMessageNotificationReceiver.NOTIFICATION_ID, 0)

        whenApiVersion(Build.VERSION_CODES.N) {
          chatNotificationSender.addReplyToExistingChatNotification(
            context,
            notificationId,
            replyText,
            sendChatMessageResponse.value.sentAt,
          )
        }

        return Result.success()
      }
    }
  }
}
