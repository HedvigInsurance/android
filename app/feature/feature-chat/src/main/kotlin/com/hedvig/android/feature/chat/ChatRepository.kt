package com.hedvig.android.feature.chat

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.toUpload
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.CacheHeaders
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.doNotStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.apollographql.apollo3.exception.ApolloCompositeException
import com.apollographql.apollo3.exception.CacheMissException
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.chat.data.ChatMessage
import com.hedvig.android.feature.chat.data.ChatMessagesResult
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.datetime.Instant
import octopus.ChatMessagesQuery
import octopus.ChatSendFileMutation
import octopus.ChatSendMessageMutation
import octopus.fragment.ChatMessageFileMessageFragment
import octopus.fragment.ChatMessageTextMessageFragment
import octopus.fragment.MessageFragment
import octopus.type.ChatMessageFileInput
import octopus.type.ChatMessageSender
import octopus.type.ChatMessageTextInput

interface ChatRepository {
  suspend fun fetchMoreMessages(until: Instant): Either<ErrorMessage, ChatMessagesResult>

  suspend fun pollNewestMessages(): Either<ErrorMessage, ChatMessagesResult>

  suspend fun watchMessages(): Flow<Either<ErrorMessage, List<ChatMessage>>>

  suspend fun sendFile(file: File, contentType: String): Either<ErrorMessage, ChatMessage>

  suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessage>
}

internal class ChatRepositoryImpl(
  private val apolloClient: ApolloClient,
) : ChatRepository {
  var failNow = false

  override suspend fun fetchMoreMessages(until: Instant): Either<ErrorMessage, ChatMessagesResult> = either {
    if (failNow) {
      failNow = false
      raise(ErrorMessage("Failing now"))
    } else {
      failNow = true
      logcat { "Stelios: Fetching more messages until $until" }
      return fetchChatMessagesQuery(until)
    }
  }

  override suspend fun pollNewestMessages(): Either<ErrorMessage, ChatMessagesResult> {
    return fetchChatMessagesQuery(null)
  }

  private suspend fun fetchChatMessagesQuery(until: Instant?): Either<ErrorMessage, ChatMessagesResult> {
    return either {
      val result = apolloClient.query(ChatMessagesQuery(until))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .doNotStore(true)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()

      populateCacheWithNewMessageData(result)

      ChatMessagesResult(
        messages = result.chat.messages.mapNotNull { it.toChatMessage() },
        nextUntil = result.chat.nextUntil,
        hasNext = result.chat.hasNext,
      )
    }
  }

  override suspend fun watchMessages(): Flow<Either<ErrorMessage, List<ChatMessage>>> {
    return apolloClient.query(ChatMessagesQuery(null))
      .fetchPolicy(FetchPolicy.CacheOnly)
      .watch()
      .retryWhen { cause, _ ->
        cause is CacheMissException ||
          (cause is ApolloCompositeException && cause.suppressedExceptions.any { it is CacheMissException })
      }
      .map { apolloResponse: ApolloResponse<ChatMessagesQuery.Data> ->
        either {
          ensure(apolloResponse.errors.isNullOrEmpty()) {
            ErrorMessage("Got errors from Apollo: ${apolloResponse.errors}")
          }
          val data: ChatMessagesQuery.Data? = apolloResponse.data
          ensureNotNull(data) {
            ErrorMessage("No data")
          }
          val chat = data.chat
          chat.messages.mapNotNull { it.toChatMessage() }
        }
          .onLeft { logcat(LogPriority.ERROR, it.throwable) { "Failed to fetch initial messages:${it.message}" } }
          .onRight { logcat { "Stelios: Successfully sending #${it.count()} messages:${it.joinToString { it.id }}" } }
      }
  }

  override suspend fun sendFile(file: File, contentType: String): Either<ErrorMessage, ChatMessage> = either {
    val fileUpload = file.toUpload(contentType)
//    val input = ChatMessageFileInput(fileUpload) // todo here upload file directly through HTTP instead
    val input = ChatMessageFileInput("")
    val mutation = ChatSendFileMutation(input)

    val result = apolloClient.mutation(mutation)
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
    if (status != null) {
      logcat(LogPriority.ERROR) { "Status was: ${status.message}" }
    }

    ensureNotNull(message.toChatMessage()) {
      ErrorMessage("Message was not of a known type")
    }
  }

  override suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessage> = either {
    val result = apolloClient.mutation(ChatSendMessageMutation(ChatMessageTextInput(text)))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val error = result.chatSendText.error
    val message = result.chatSendText.message
    val status = result.chatSendText.status

    ensure(error == null) {
      ErrorMessage("Failed with error response: ${error?.message}")
    }
    ensureNotNull(message) {
      ErrorMessage("No data")
    }
    // todo check what status should be used for. Now it prints stuff like "Chat is closed, we'll answer asap"
    if (status != null) {
      logcat(LogPriority.WARN) { "Status was: ${status.message}" }
    }

    val chatMessage = ensureNotNull(message.toChatMessage()) {
      ErrorMessage("Message was not of a known type")
    }
    populateCacheWithNewMessage(
      ChatMessagesQuery.Data.Chat.ChatMessageTextMessage(
        __typename = octopus.type.ChatMessageText.type.name,
        id = chatMessage.id,
        sender = when (chatMessage.sender) {
          ChatMessage.Sender.MEMBER -> ChatMessageSender.MEMBER
          ChatMessage.Sender.HEDVIG -> ChatMessageSender.HEDVIG
        },
        sentAt = chatMessage.sentAt,
        text = (chatMessage as ChatMessage.ChatMessageText).text,
      ),
    )
    chatMessage
  }

  private suspend fun populateCacheWithNewMessage(newMessage: ChatMessagesQuery.Data.Chat.ChatMessageTextMessage) {
    val existingData: ChatMessagesQuery.Data = try {
      apolloClient.apolloStore.readOperation(ChatMessagesQuery(null), apolloClient.customScalarAdapters)
    } catch (e: CacheMissException) {
      // If we have no data in the cache, we can just ignore eagerly populating our cache with the message which we've
      // just sent
      return
    }
    mergeAndWriteMessagesToCache(existingData, listOf(newMessage))
  }

  private suspend fun populateCacheWithNewMessageData(queryData: ChatMessagesQuery.Data) {
    val existingData: ChatMessagesQuery.Data = try {
      apolloClient.apolloStore.readOperation(ChatMessagesQuery(null), apolloClient.customScalarAdapters)
    } catch (e: CacheMissException) {
      // If there is nothing in the cache in the first place, we can just populate the cache with the entire response
      queryData
    }
    mergeAndWriteMessagesToCache(existingData, queryData.chat.messages)
  }

  private suspend fun mergeAndWriteMessagesToCache(
    existingData: ChatMessagesQuery.Data,
    newMessages: List<ChatMessagesQuery.Data.Chat.Message>,
  ) {
    val mergedMessages = (existingData.chat.messages + newMessages).distinctBy { it.id }
    val modifiedData = existingData.copy(existingData.chat.copy(messages = mergedMessages))
    apolloClient.apolloStore.writeOperation(
      ChatMessagesQuery(null),
      modifiedData,
      apolloClient.customScalarAdapters,
      CacheHeaders.NONE,
      true,
    )
  }
}

private fun MessageFragment.toChatMessage(): ChatMessage? = when (this) {
  is ChatMessageFileMessageFragment -> ChatMessage.ChatMessageFile(
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

  is ChatMessageTextMessageFragment -> ChatMessage.ChatMessageText(
    id = id,
    sender = when (sender) {
      ChatMessageSender.MEMBER -> ChatMessage.Sender.MEMBER
      ChatMessageSender.HEDVIG -> ChatMessage.Sender.HEDVIG
      ChatMessageSender.UNKNOWN__ -> ChatMessage.Sender.HEDVIG
    },
    sentAt = sentAt,
    text = text,
  )

  else -> {
    logcat(LogPriority.WARN) { "Got unknown message type, can not map message:$this" }
    null
  }
}
