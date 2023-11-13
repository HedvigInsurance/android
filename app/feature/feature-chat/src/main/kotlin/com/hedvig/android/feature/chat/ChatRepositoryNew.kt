package com.hedvig.android.feature.chat

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.toUpload
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.data.ChatMessage
import com.hedvig.android.feature.chat.data.ChatMessageResult
import com.hedvig.android.feature.chat.data.ChatMessagesResult
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.io.File
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

  suspend fun sendFile(file: File, contentType: String): Either<ErrorMessage, ChatMessageResult>

  suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessageResult>
}

internal class ChatRepositoryNewImpl(
  private val apolloClientOctopus: ApolloClient,
) : ChatRepositoryNew {
  override suspend fun fetchChatMessages(until: Instant?) = either {
    val result = apolloClientOctopus.query(ChatMessagesQuery(until))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    ChatMessagesResult(
      messages = result.chat.messages.mapNotNull { it.toMessage() }.reversed(),
      nextUntil = result.chat.nextUntil,
      hasNext = result.chat.hasNext,
    )
  }

  override suspend fun sendFile(file: File, contentType: String) = either {
    val fileUpload = file.toUpload(contentType)
//    val input = ChatMessageFileInput(fileUpload) // todo here upload file directly through HTTP instead
    val input = ChatMessageFileInput("")
    val mutation = ChatSendFileMutation(input)

    val result = apolloClientOctopus.mutation(mutation)
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val error = result.chatSendFile.error
    val message = result.chatSendFile.message
    val status = result.chatSendFile.status

    ensure(error != null) {
      ErrorMessage(error?.message)
    }
    ensureNotNull(message) {
      ErrorMessage("No data")
    }

    ChatMessageResult(
      message = message.toMessage(),
      status = status?.message ?: "",
    )
  }

  override suspend fun sendMessage(text: String) = either {
    val result = apolloClientOctopus.mutation(ChatSendMessageMutation(ChatMessageTextInput(text)))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val error = result.chatSendText.error
    val message = result.chatSendText.message
    val status = result.chatSendText.status

    ensure(error != null) {
      ErrorMessage(error?.message)
    }
    ensureNotNull(message) {
      ErrorMessage("No data")
    }

    ChatMessageResult(
      message = message.toMessage(),
      status = status?.message ?: "",
    )
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
    sentAt = sentAt,
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
    sentAt = sentAt,
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
