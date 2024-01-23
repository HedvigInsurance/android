package com.hedvig.android.feature.chat.data

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.feature.chat.model.ChatMessagesResult
import com.hedvig.android.navigation.core.AppDestination
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class ChatRepositoryDemo(
  private val clock: Clock,
) : ChatRepository {
  private val messages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage.ChatMessageText(
        id = Uuid.randomUUID().toString(),
        text = "Hello, this is not a real chat, you are in demo mode.",
        sender = ChatMessage.Sender.HEDVIG,
        sentAt = clock.now() - 1.seconds,
      ),
      ChatMessage.ChatMessageText(
        id = Uuid.randomUUID().toString(),
        text = "You can send messages, but they will not be received by anyone.",
        sender = ChatMessage.Sender.HEDVIG,
        sentAt = clock.now(),
      ),
    ),
  )

  override suspend fun fetchMoreMessages(until: Instant): Either<ErrorMessage, ChatMessagesResult> {
    return ErrorMessage("Demo mode").left()
  }

  override suspend fun pollNewestMessages(): Either<ErrorMessage, ChatMessagesResult> {
    return ErrorMessage("Demo mode").left()
  }

  override fun watchMessages(): Flow<Either<ErrorMessage, List<ChatMessage>>> {
    return messages.map { it.right() }
  }

  override suspend fun sendPhoto(uri: Uri, context: AppDestination.Chat.ChatContext?): Either<ErrorMessage, ChatMessage> {
    return ErrorMessage("Demo mode").left()
  }

  override suspend fun sendMedia(uri: Uri, context: AppDestination.Chat.ChatContext?): Either<ErrorMessage, ChatMessage> {
    return ErrorMessage("Demo mode").left()
  }

  override suspend fun sendMessage(text: String, context: AppDestination.Chat.ChatContext?): Either<ErrorMessage, ChatMessage> {
    val chatMessage = ChatMessage.ChatMessageText(
      id = Uuid.randomUUID().toString(),
      text = text,
      sender = ChatMessage.Sender.MEMBER,
      sentAt = clock.now(),
    )
    messages.update { it.plus(chatMessage) }
    return chatMessage.right()
  }
}
