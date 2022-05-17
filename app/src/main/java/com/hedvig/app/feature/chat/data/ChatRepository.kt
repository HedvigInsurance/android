package com.hedvig.app.feature.chat.data

import android.content.Context
import android.net.Uri
import arrow.core.Either
import arrow.core.flatMap
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.FileUpload
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.cache.http.HttpCachePolicy
import com.apollographql.apollo3.cache.http.HttpFetchPolicy
import com.apollographql.apollo3.cache.http.httpFetchPolicy
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.refetchPolicy
import com.apollographql.apollo3.coroutines.await
import com.apollographql.apollo3.coroutines.toFlow
import com.apollographql.apollo3.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.graphql.ChatMessageIdQuery
import com.hedvig.android.owldroid.graphql.ChatMessageSubscription
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.graphql.EditLastResponseMutation
import com.hedvig.android.owldroid.graphql.GifQuery
import com.hedvig.android.owldroid.graphql.SendChatFileResponseMutation
import com.hedvig.android.owldroid.graphql.SendChatSingleSelectResponseMutation
import com.hedvig.android.owldroid.graphql.SendChatTextResponseMutation
import com.hedvig.android.owldroid.graphql.TriggerFreeTextChatMutation
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.android.owldroid.type.ChatResponseBodyFileInput
import com.hedvig.android.owldroid.type.ChatResponseBodySingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseBodyTextInput
import com.hedvig.android.owldroid.type.ChatResponseFileInput
import com.hedvig.android.owldroid.type.ChatResponseSingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseTextInput
import com.hedvig.app.service.FileService
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.extensions.into
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
            .httpFetchPolicy(HttpFetchPolicy.NetworkOnly)
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .watcher()
            .toFlow()
    }

    suspend fun messageIds() =
        apolloClient
            .query(ChatMessageIdQuery())
            .httpFetchPolicy(HttpFetchPolicy.NetworkOnly)
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()

    fun subscribeToChatMessages() =
        apolloClient.subscription(ChatMessageSubscription()).toFlow()

    suspend fun sendChatMessage(
        id: String,
        message: String,
    ) = apolloClient.mutation(
        SendChatTextResponseMutation(
            ChatResponseTextInput(
                id,
                ChatResponseBodyTextInput(message)
            )
        )
    ).execute()

    suspend fun sendSingleSelect(
        id: String,
        value: String,
    ) = apolloClient.mutation(
        SendChatSingleSelectResponseMutation(
            ChatResponseSingleSelectInput(id, ChatResponseBodySingleSelectInput(value))
        )
    ).execute()

    fun writeNewMessage(message: ChatMessageFragment) {
        val cachedData = apolloClient
            .apolloStore
            .read(messagesQuery)
            .execute()

        val chatMessagesFragment =
            ChatMessagesQuery
                .Message
                .Fragments(chatMessageFragment = message)

        val newMessages = cachedData.messages.toMutableList()
        newMessages.add(
            0,
            ChatMessagesQuery.Message(
                message.__typename,
                fragments = chatMessagesFragment
            )
        )

        val newData = cachedData
            .copy(messages = newMessages)

        apolloClient
            .apolloStore
            .writeAndPublish(messagesQuery, newData)
            .execute()
    }

    suspend fun uploadFileFromProvider(uri: Uri): ApolloResponse<UploadFileMutation.Data> {
        val mimeType = fileService.getMimeType(uri)
        val file = File(
            context.cacheDir,
            fileService.getFileName(uri)
                ?: "${UUID.randomUUID()}.${fileService.getFileExtension(uri.toString())}"
        ) // I hate this but it seems there's no other way
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.into(file)
            return@withContext uploadFile(file.path, mimeType)
        }
    }

    suspend fun uploadFile(uri: Uri): ApolloResponse<UploadFileMutation.Data> =
        uploadFile(uri.path!!, fileService.getMimeType(uri))

    private suspend fun uploadFile(
        path: String,
        mimeType: String,
    ): ApolloResponse<UploadFileMutation.Data> {
        val uploadFileMutation = UploadFileMutation(
            file = FileUpload(mimeType, path)
        )

        return apolloClient.mutation(uploadFileMutation).execute()
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
                mimeType = mimeType
            ),
            globalId = id
        )

        val chatFileResponse = SendChatFileResponseMutation(input)

        return apolloClient.mutation(chatFileResponse).execute()
    }

    suspend fun editLastResponse(): ApolloResponse<EditLastResponseMutation.Data> =
        apolloClient.mutation(EditLastResponseMutation()).execute()

    suspend fun triggerFreeTextChat(): Either<FreeTextError, FreeTextSuccess> =
        apolloClient.mutation(TriggerFreeTextChatMutation())
            .safeQuery()
            .toEither { FreeTextError.NetworkError }
            .flatMap { data ->
                val didTriggerFreeTextChat = data.triggerFreeTextChat ?: false

                Either.conditionally(
                    didTriggerFreeTextChat,
                    ifFalse = { FreeTextError.CouldNotTrigger },
                    ifTrue = { FreeTextSuccess }
                )
            }

    suspend fun searchGifs(query: String): ApolloResponse<GifQuery.Data> =
        apolloClient.query(GifQuery(query)).execute()
}

sealed class FreeTextError {
    object NetworkError : FreeTextError()
    object CouldNotTrigger : FreeTextError()
}

object FreeTextSuccess
