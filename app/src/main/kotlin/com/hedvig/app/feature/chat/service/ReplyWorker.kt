package com.hedvig.app.feature.chat.service

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.app.feature.chat.data.ChatRepository
import slimber.log.e

class ReplyWorker(
  private val context: Context,
  params: WorkerParameters,
  private val chatRepository: ChatRepository,
  private val chatNotificationSender: ChatNotificationSender,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val replyText = inputData.getString(ChatMessageNotificationReceiver.REPLY_TEXT)
      ?: return Result.failure()

    val idsResponse = runCatching {
      chatRepository
        .messageIds()
    }

    if (idsResponse.isFailure) {
      idsResponse.exceptionOrNull()?.let { e(it) }
      return Result.failure()
    }

    val lastChatMessage =
      idsResponse.getOrNull()?.data?.messages?.first() ?: return Result.failure()
    val sendChatMessageResponse = runCatching {
      chatRepository
        .sendChatMessage(
          lastChatMessage.globalId,
          replyText,
        )
    }

    if (sendChatMessageResponse.isFailure) {
      sendChatMessageResponse.exceptionOrNull()?.let { e(it) }
      return Result.failure()
    }

    val notificationId =
      inputData.getInt(ChatMessageNotificationReceiver.NOTIFICATION_ID, 0)

    whenApiVersion(Build.VERSION_CODES.N) {
      chatNotificationSender.addReplyToExistingChatNotification(
        context,
        notificationId,
        replyText,
      )
    }

    return Result.success()
  }
}
