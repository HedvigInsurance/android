package com.hedvig.app.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.Instant
import octopus.ChatMessagesQuery
import octopus.ChatSendFileMutation
import octopus.ChatSendMessageMutation
import octopus.fragment.MessageFragment
import octopus.type.ChatMessageFileInput
import octopus.type.ChatMessageSender
import octopus.type.ChatMessageTextInput

interface ChatRepositoryNew {
  suspend fun fetchChatMessages(until: Instant? = null): Either<ErrorMessage, ChatMessagesResult>
  suspend fun sendFile(uploadUrl: String): Either<ErrorMessage, ChatMessageResult>
  suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessageResult>
}

class ChatRepositoryNewImpl(
  private val apolloClientOctopus: ApolloClient,
) {
  suspend fun fetchChatMessages(until: Instant? = null) = either {
    val result = apolloClientOctopus.query(ChatMessagesQuery(until))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    ChatMessagesResult(
      messages = result.chat.messages.mapNotNull { it.toMessage() },
      nextUntil = result.chat.nextUntil,
      hasNext = result.chat.hasNext,
    )
  }

  suspend fun sendFile(uploadUrl: String) = either {
    val result = apolloClientOctopus.mutation(ChatSendFileMutation(ChatMessageFileInput(uploadUrl)))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val error = result.chatSendFile.error
    val message = result.chatSendFile.message
    val status = result.chatSendFile.status

    if (error != null) {
      raise(ErrorMessage(error.message))
    } else if (message == null) {
      raise(ErrorMessage("No data"))
    } else {
      ChatMessageResult(
        message = message.toMessage(),
        status = status?.message ?: "",
      )
    }
  }

  suspend fun sendMessage(text: String) = either {
    val result = apolloClientOctopus.mutation(ChatSendMessageMutation(ChatMessageTextInput(text)))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val error = result.chatSendText.error
    val message = result.chatSendText.message
    val status = result.chatSendText.status

    if (error != null) {
      raise(ErrorMessage(error.message))
    } else if (message == null) {
      raise(ErrorMessage("No data"))
    } else {
      ChatMessageResult(
        message = message.toMessage(),
        status = status?.message ?: "",
      )
    }
  }
}

private fun MessageFragment.toMessage() = when (this) {
  is ChatMessagesQuery.Data.Chat.ChatMessageFileMessage -> ChatMessage.ChatMessageFile(
    id = id,
    sender = when (sender) {
      ChatMessageSender.MEMBER -> ChatMessage.Sender.MEMBER
      ChatMessageSender.HEDVIG -> ChatMessage.Sender.HEDVIG
      ChatMessageSender.UNKNOWN__ -> ChatMessage.Sender.HEDVIG
    },
    sentAt = null,
    url = signedUrl,
    mimeType = mimeType,
  )

  is ChatMessagesQuery.Data.Chat.ChatMessageTextMessage -> ChatMessage.ChatMessageText(
    id = id,
    sender = when (sender) {
      ChatMessageSender.MEMBER -> ChatMessage.Sender.MEMBER
      ChatMessageSender.HEDVIG -> ChatMessage.Sender.HEDVIG
      ChatMessageSender.UNKNOWN__ -> ChatMessage.Sender.HEDVIG
    },
    sentAt = null,
    text = text,
  )

  is ChatMessagesQuery.Data.Chat.OtherMessage -> {
    logcat(LogPriority.WARN) { "Got OtherMessage message type, can not map message" }
    null
  }

  else -> {
    logcat(LogPriority.WARN) { "Got unknown message type, can not map message" }
    null
  }
}
