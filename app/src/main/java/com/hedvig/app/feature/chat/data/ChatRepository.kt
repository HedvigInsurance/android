package com.hedvig.app.feature.chat.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
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
import com.hedvig.android.owldroid.graphql.UploadClaimMutation
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.android.owldroid.type.ChatResponseBodyFileInput
import com.hedvig.android.owldroid.type.ChatResponseBodySingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseBodyTextInput
import com.hedvig.android.owldroid.type.ChatResponseFileInput
import com.hedvig.android.owldroid.type.ChatResponseSingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseTextInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ChatRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val fileService: FileService,
    private val context: Context
) {
    private lateinit var messagesQuery: ChatMessagesQuery

    fun fetchChatMessages(): Flow<Response<ChatMessagesQuery.Data>> {
        messagesQuery = ChatMessagesQuery()
        return apolloClientWrapper.apolloClient
            .query(messagesQuery)
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .watcher()
            .toFlow()
    }

    suspend fun messageIds() = apolloClientWrapper
        .apolloClient
        .query(ChatMessageIdQuery())
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()

    fun subscribeToChatMessages() =
        apolloClientWrapper.apolloClient.subscribe(ChatMessageSubscription()).toFlow()

    suspend fun sendChatMessage(
        id: String,
        message: String
    ) = apolloClientWrapper.apolloClient.mutate(
        SendChatTextResponseMutation(
            ChatResponseTextInput(
                id,
                ChatResponseBodyTextInput(message)
            )
        )
    ).await()

    suspend fun sendSingleSelect(
        id: String,
        value: String
    ) = apolloClientWrapper.apolloClient.mutate(
        SendChatSingleSelectResponseMutation(
            ChatResponseSingleSelectInput(id, ChatResponseBodySingleSelectInput(value))
        )
    ).await()

    suspend fun uploadClaim(id: String, path: String): Response<UploadClaimMutation.Data> {
        val mutation = UploadClaimMutation(
            id = id,
            claim = FileUpload(fileService.getMimeType(path), path)
        )

        return apolloClientWrapper.apolloClient.mutate(mutation).await()
    }

    fun writeNewMessage(message: ChatMessageFragment) {
        val cachedData = apolloClientWrapper.apolloClient
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

        apolloClientWrapper.apolloClient
            .apolloStore
            .writeAndPublish(messagesQuery, newData)
            .execute()
    }

    suspend fun uploadFileFromProvider(uri: Uri): Response<UploadFileMutation.Data> {
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

    suspend fun uploadFile(uri: Uri): Response<UploadFileMutation.Data> =
        uploadFile(uri.path!!, fileService.getMimeType(uri))

    private suspend fun uploadFile(
        path: String,
        mimeType: String
    ): Response<UploadFileMutation.Data> {
        val uploadFileMutation = UploadFileMutation(
            file = FileUpload(mimeType, path)
        )

        return apolloClientWrapper.apolloClient.mutate(uploadFileMutation).await()
    }

    suspend fun sendFileResponse(
        id: String,
        key: String,
        uri: Uri
    ): Response<SendChatFileResponseMutation.Data> {
        val mimeType = fileService.getMimeType(uri)

        val input = ChatResponseFileInput(
            body = ChatResponseBodyFileInput(
                key = key,
                mimeType = mimeType
            ),
            globalId = id
        )

        val chatFileResponse = SendChatFileResponseMutation(input)

        return apolloClientWrapper.apolloClient.mutate(chatFileResponse).await()
    }

    suspend fun editLastResponse() =
        apolloClientWrapper.apolloClient.mutate(EditLastResponseMutation()).await()

    suspend fun triggerFreeTextChat() =
        apolloClientWrapper.apolloClient.mutate(TriggerFreeTextChatMutation()).await()

    suspend fun searchGifs(query: String) =
        apolloClientWrapper.apolloClient.query(GifQuery(query)).await()
}
