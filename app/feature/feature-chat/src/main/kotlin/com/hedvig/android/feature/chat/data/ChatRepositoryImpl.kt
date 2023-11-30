package com.hedvig.android.feature.chat.data

import android.net.Uri
import android.util.Patterns
import androidx.core.net.toFile
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
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
import com.hedvig.android.core.retrofit.toErrorMessage
import com.hedvig.android.feature.chat.FileService
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.feature.chat.model.ChatMessagesResult
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
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
import octopus.type.ChatMessageSender
import okhttp3.MediaType.Companion.toMediaType

internal class ChatRepositoryImpl(
  private val apolloClient: ApolloClient,
  private val botServiceService: BotServiceService,
  private val fileService: FileService,
) : ChatRepository {
  override suspend fun fetchMoreMessages(until: Instant): Either<ErrorMessage, ChatMessagesResult> = either {
    logcat { "Fetching more messages until:$until" }
    return fetchChatMessagesQuery(until)
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
        messages = result.chat.messages.mapNotNull { it.toUiChatMessage() },
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
          chat.messages.mapNotNull { it.toUiChatMessage() }
        }
          .onLeft { logcat(LogPriority.ERROR, it.throwable) { "Failed to fetch initial messages:${it.message}" } }
          .onRight { logcat { "Watching #${it.count()} messages" } }
      }
  }

  override suspend fun sendFile(uri: Uri): Either<ErrorMessage, ChatMessage> = either<ErrorMessage, ChatMessage> {
    logcat { "Chat uploading file with uri:$uri" }
    val uploadToken = uploadFile(uri)
    val result = apolloClient.mutation(ChatSendFileMutation(uploadToken))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()
      .chatSendFile

    val error = result.error
    val message = result.message
    val status = result.status

    ensure(error == null) {
      ErrorMessage("Uploading file failed with error message:${error?.message}. Result:$result")
    }
    ensureNotNull(message) {
      ErrorMessage("No data")
    }
    if (status != null) {
      logcat { "Status was: ${status.message}" }
    }

    ensureNotNull(message.toUiChatMessage()) {
      ErrorMessage("Message was not of a known type")
    }
  }.onLeft {
    logcat { "Stelios: error uploading file:$it" }
  }

  private suspend fun Raise<ErrorMessage>.uploadFile(uri: Uri): String {
    val contentType = fileService.getMimeType(uri).toMediaType()
    val file = uri.toFile()
    val result = botServiceService.uploadFile(file, contentType)
      .onLeft {
        logcat(LogPriority.ERROR) { "Failed to upload file with path:${file.absolutePath}. Error:$it" }
      }
      .mapLeft(CallError::toErrorMessage)
      .bind()
    logcat { "Stelios: Uploaded file with path:${file.absolutePath}. Result:$result" }
    val fileUploadResponse = result.firstOrNull() ?: raise(ErrorMessage("No file upload response"))
    return ensureNotNull(fileUploadResponse.uploadToken) {
      ErrorMessage("Backend responded with an empty list as a response$result")
    }
  }

  override suspend fun sendMessage(text: String): Either<ErrorMessage, ChatMessage> = either {
    val result = apolloClient.mutation(ChatSendMessageMutation(text))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()
      .chatSendText

    val error = result.error
    val message = result.message
    val status = result.status

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

    val chatMessage = ensureNotNull(message.toUiChatMessage()) {
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

private fun MessageFragment.toUiChatMessage(): ChatMessage? = when (this) {
  is ChatMessageFileMessageFragment -> ChatMessage.ChatMessageFile(
    id = id,
    sender = sender.toChatMessageSender(),
    sentAt = sentAt,
    url = signedUrl,
    mimeType = mimeType,
  )

  is ChatMessageTextMessageFragment -> {
    if (text.isGifUrl()) {
      ChatMessage.ChatMessageGif(
        id = id,
        sender = sender.toChatMessageSender(),
        sentAt = sentAt,
        gifUrl = text,
      )
    } else {
      ChatMessage.ChatMessageText(
        id = id,
        sender = sender.toChatMessageSender(),
        sentAt = sentAt,
        text = text,
      )
    }
  }

  else -> {
    logcat(LogPriority.WARN) { "Got unknown message type, can not map message:$this" }
    null
  }
}

private fun ChatMessageSender.toChatMessageSender(): ChatMessage.Sender = when (this) {
  ChatMessageSender.MEMBER -> ChatMessage.Sender.MEMBER
  ChatMessageSender.HEDVIG -> ChatMessage.Sender.HEDVIG
  ChatMessageSender.UNKNOWN__ -> ChatMessage.Sender.HEDVIG
}

private fun String.isGifUrl(): Boolean {
  if (!endsWith(".gif")) return false
  return webUrlLinkMatcher.matchEntire(this) != null
}

private val webUrlLinkMatcher: Regex = Patterns.WEB_URL.toRegex()