package com.hedvig.app.feature.chat.service

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.util.whenApiVersion
import e
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReplyWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params),
    KoinComponent {
    private val chatRepository: ChatRepository by inject()
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
                    replyText
                )
        }

        if (sendChatMessageResponse.isFailure) {
            sendChatMessageResponse.exceptionOrNull()?.let { e(it) }
            return Result.failure()
        }

        val notificationId =
            inputData.getInt(ChatMessageNotificationReceiver.NOTIFICATION_ID, 0)

        whenApiVersion(Build.VERSION_CODES.N) {
            ChatNotificationManager.addReplyToExistingChatNotification(
                context,
                notificationId,
                replyText
            )
        }

        return Result.success()
    }
}
