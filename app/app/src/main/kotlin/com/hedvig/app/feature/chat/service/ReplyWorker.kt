package com.hedvig.app.feature.chat.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hedvig.android.feature.chat.ChatRepositoryNew

class ReplyWorker(
  private val context: Context,
  params: WorkerParameters,
  private val chatRepository: ChatRepositoryNew,
  private val chatNotificationSender: ChatNotificationSender,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val replyText = inputData.getString(ChatMessageNotificationReceiver.REPLY_TEXT)
      ?: return Result.failure()
// todo adjust to new repository
    return Result.failure()
//    val idsResponse = runCatching {
//      chatRepository
//        .messageIds()
//    }
//
//    if (idsResponse.isFailure) {
//      idsResponse.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "messages ids failed to load" } }
//      return Result.failure()
//    }
//
//    val lastChatMessage =
//      idsResponse.getOrNull()?.data?.messages?.first() ?: return Result.failure()
//    val sendChatMessageResponse = chatRepository.sendChatMessage(
//      lastChatMessage.globalId,
//      replyText,
//    )
//
//    if (sendChatMessageResponse is Either.Left) {
//      val error = sendChatMessageResponse.value
//      logcat(LogPriority.ERROR, error.throwable) {
//        buildString {
//          append("Chat: Replying through ReplyWorker failed")
//          if (error.message != null) {
//            append(". Message:${error.message}")
//          }
//        }
//      }
//      return Result.failure()
//    }
//
//    val notificationId =
//      inputData.getInt(ChatMessageNotificationReceiver.NOTIFICATION_ID, 0)
//
//    whenApiVersion(Build.VERSION_CODES.N) {
//      chatNotificationSender.addReplyToExistingChatNotification(
//        context,
//        notificationId,
//        replyText,
//      )
//    }
//
//    return Result.success()
  }
}
