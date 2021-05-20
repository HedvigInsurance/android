package com.hedvig.app.feature.chat.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.ApolloClient
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
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val fileService: FileService,
    @ApplicationContext private val context: Context,
) {
    private lateinit var messagesQuery: ChatMessagesQuery

    fun fetchChatMessages(): Flow<Response<ChatMessagesQuery.Data>> {
        messagesQuery = ChatMessagesQuery()
        return apolloClient
            .query(messagesQuery)
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .watcher()
            .toFlow()
    }

    suspend fun messageIds() =
        apolloClient
            .query(ChatMessageIdQuery())
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .await()

    fun subscribeToChatMessages() =
        apolloClient.subscribe(ChatMessageSubscription()).toFlow()

    suspend fun sendChatMessage(
        id: String,
        message: String,
    ) = apolloClient.mutate(
        SendChatTextResponseMutation(
            ChatResponseTextInput(
                id,
                ChatResponseBodyTextInput(message)
            )
        )
    ).await()

    suspend fun sendSingleSelect(
        id: String,
        value: String,
    ) = apolloClient.mutate(
        SendChatSingleSelectResponseMutation(
            ChatResponseSingleSelectInput(id, ChatResponseBodySingleSelectInput(value))
        )
    ).await()

    suspend fun uploadClaim(id: String, path: String): Response<UploadClaimMutation.Data> {
        val mutation = UploadClaimMutation(
            id = id,
            claim = FileUpload(fileService.getMimeType(path), path)
        )

        return apolloClient.mutate(mutation).await()
    }

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
        mimeType: String,
    ): Response<UploadFileMutation.Data> {
        val uploadFileMutation = UploadFileMutation(
            file = FileUpload(mimeType, path)
        )

        return apolloClient.mutate(uploadFileMutation).await()
    }

    suspend fun sendFileResponse(
        id: String,
        key: String,
        uri: Uri,
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

        return apolloClient.mutate(chatFileResponse).await()
    }

    suspend fun editLastResponse() =
        apolloClient.mutate(EditLastResponseMutation()).await()

    suspend fun triggerFreeTextChat() =
        apolloClient.mutate(TriggerFreeTextChatMutation()).await()

    suspend fun searchGifs(query: String) =
        apolloClient.query(GifQuery(query)).await()
}
