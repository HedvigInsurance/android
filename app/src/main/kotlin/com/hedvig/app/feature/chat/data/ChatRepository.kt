package com.hedvig.app.feature.chat.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.toUpload
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import giraffe.ChatMessageIdQuery
import giraffe.ChatMessageSubscription
import giraffe.ChatMessagesQuery
import giraffe.EditLastResponseMutation
import giraffe.GifQuery
import giraffe.SendChatFileResponseMutation
import giraffe.SendChatSingleSelectResponseMutation
import giraffe.SendChatTextResponseMutation
import giraffe.TriggerFreeTextChatMutation
import giraffe.UploadFileMutation
import giraffe.fragment.ChatMessageFragment
import giraffe.type.ChatResponseBodyFileInput
import giraffe.type.ChatResponseBodySingleSelectInput
import giraffe.type.ChatResponseBodyTextInput
import giraffe.type.ChatResponseFileInput
import giraffe.type.ChatResponseSingleSelectInput
import giraffe.type.ChatResponseTextInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ChatRepository(
  private val apolloClient: ApolloClient,
  private val fileService: FileService,
  private val context: Context,
) {
  private lateinit var messagesQuery: ChatMessagesQuery

  fun fetchChatMessages(): Flow<ApolloResponse<ChatMessagesQuery.Data>> {
    messagesQuery = ChatMessagesQuery()
    return apolloClient
      .query(messagesQuery)
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .watch()
  }

  suspend fun messageIds() =
    apolloClient
      .query(ChatMessageIdQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .execute()

  fun subscribeToChatMessages() =
    apolloClient.subscription(ChatMessageSubscription()).toFlow()

  suspend fun sendChatMessage(
    id: String,
    message: String,
  ): Either<OperationResult.Error, SendChatTextResponseMutation.Data> {
    return apolloClient
      .mutation(
        SendChatTextResponseMutation(
          ChatResponseTextInput(
            id,
            ChatResponseBodyTextInput(message),
          ),
        ),
      )
      .safeExecute()
      .toEither()
  }

  suspend fun sendSingleSelect(
    id: String,
    value: String,
  ) = apolloClient.mutation(
    SendChatSingleSelectResponseMutation(
      ChatResponseSingleSelectInput(id, ChatResponseBodySingleSelectInput(value)),
    ),
  ).execute()

  suspend fun writeNewMessage(message: ChatMessageFragment) {
    val cachedData = apolloClient
      .apolloStore
      .readOperation(messagesQuery)

    val chatMessagesFragment =
      ChatMessagesQuery
        .Message
        .Fragments(chatMessageFragment = message)

    val newMessages = cachedData.messages.toMutableList()
    newMessages.add(
      0,
      ChatMessagesQuery.Message(
        message.body.__typename,
        fragments = chatMessagesFragment,
      ),
    )

    val newData = cachedData
      .copy(messages = newMessages)

    apolloClient
      .apolloStore
      .writeOperation(messagesQuery, newData)
  }

  @SuppressLint("Recycle")
  suspend fun uploadFileFromProvider(uri: Uri): Either<OperationResult.Error, UploadFileMutation.Data> {
    val mimeType = fileService.getMimeType(uri)
    val file = File(
      context.cacheDir,
      fileService.getFileName(uri)
        ?: "${UUID.randomUUID()}.${fileService.getFileExtension(uri.toString())}",
    ) // I hate this but it seems there's no other way
    return withContext(Dispatchers.IO) {
      context.contentResolver.openInputStream(uri)?.into(file)
      return@withContext uploadFile(file, mimeType)
    }
  }

  suspend fun uploadFile(uri: Uri): Either<OperationResult.Error, UploadFileMutation.Data> =
    uploadFile(File(uri.path!!), fileService.getMimeType(uri))

  private suspend fun uploadFile(
    file: File,
    mimeType: String,
  ): Either<OperationResult.Error, UploadFileMutation.Data> {
    return apolloClient.mutation(UploadFileMutation(file.toUpload(mimeType))).safeExecute().toEither()
  }

  suspend fun sendFileResponse(
    id: String,
    key: String,
    uri: Uri,
  ): ApolloResponse<SendChatFileResponseMutation.Data> {
    val mimeType = fileService.getMimeType(uri)

    val input = ChatResponseFileInput(
      body = ChatResponseBodyFileInput(
        key = key,
        mimeType = mimeType,
      ),
      globalId = id,
    )

    val chatFileResponse = SendChatFileResponseMutation(input)

    return apolloClient.mutation(chatFileResponse).execute()
  }

  suspend fun editLastResponse(): ApolloResponse<EditLastResponseMutation.Data> =
    apolloClient.mutation(EditLastResponseMutation()).execute()

  suspend fun triggerFreeTextChat(): Either<FreeTextError, FreeTextSuccess> {
    return either {
      val data = apolloClient
        .mutation(TriggerFreeTextChatMutation())
        .safeExecute()
        .toEither { FreeTextError.NetworkError }
        .bind()
      val didTriggerFreeTextChat = data.triggerFreeTextChat == true
      ensure(didTriggerFreeTextChat) { FreeTextError.CouldNotTrigger }
      FreeTextSuccess
    }
  }

  suspend fun searchGifs(query: String): ApolloResponse<GifQuery.Data> =
    apolloClient.query(GifQuery(query)).execute()
}

sealed class FreeTextError {
  object NetworkError : FreeTextError()
  object CouldNotTrigger : FreeTextError()
}

object FreeTextSuccess
