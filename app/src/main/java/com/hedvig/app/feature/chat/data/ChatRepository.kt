package com.hedvig.app.feature.chat.data

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
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
import com.hedvig.app.util.extensions.toDeferred
import com.hedvig.app.util.extensions.toFlow
import kotlinx.coroutines.flow.Flow
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
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .watcher()
            .toFlow()
    }

    fun subscribeToChatMessages() =
        apolloClientWrapper.apolloClient.subscribe(ChatMessageSubscription()).toFlow()

    suspend fun sendChatMessage(
        id: String,
        message: String
    ): Response<SendChatTextResponseMutation.Data> {
        val input = ChatResponseTextInput(
            globalId = id,
            body = ChatResponseBodyTextInput(text = message)
        )

        val sendChatMessageMutation =
            SendChatTextResponseMutation(input = input)

        return apolloClientWrapper.apolloClient.mutate(sendChatMessageMutation).toDeferred().await()
    }

    suspend fun sendSingleSelect(
        id: String,
        value: String
    ): Response<SendChatSingleSelectResponseMutation.Data> {
        val input = ChatResponseSingleSelectInput(
            globalId = id,
            body = ChatResponseBodySingleSelectInput(
                selectedValue = value
            )
        )

        val sendChatSingleSelectMutation = SendChatSingleSelectResponseMutation(
            input = input
        )

        return apolloClientWrapper.apolloClient.mutate(sendChatSingleSelectMutation).toDeferred()
            .await()
    }

    suspend fun uploadClaim(id: String, path: String): Response<UploadClaimMutation.Data> {
        val mutation = UploadClaimMutation(
            id = id,
            claim = FileUpload(fileService.getMimeType(path), File(path))
        )

        return apolloClientWrapper.apolloClient.mutate(mutation).toDeferred().await()
    }

    fun writeNewMessage(message: ChatMessageFragment) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(messagesQuery)
            .execute()

        val chatMessagesFragment =
            ChatMessagesQuery
                .Message
                .Fragments(chatMessageFragment = message)

        val newMessages = cachedData.messages.toMutableList()
        newMessages.add(
            ChatMessagesQuery.Message(
                message.__typename,
                fragments = chatMessagesFragment
            )
        )

        val newData = cachedData
            .copy(messages = newMessages)

        apolloClientWrapper.apolloClient
            .apolloStore()
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
        context.contentResolver.openInputStream(uri)?.into(file)
        return uploadFile(file, mimeType ?: "")
    }

    suspend fun uploadFile(uri: Uri): Response<UploadFileMutation.Data> =
        uploadFile(File(uri.path), fileService.getMimeType(uri) ?: "")

    private suspend fun uploadFile(
        file: File,
        mimeType: String
    ): Response<UploadFileMutation.Data> {
        val uploadFileMutation = UploadFileMutation(
            file = FileUpload(mimeType, file)
        )

        return apolloClientWrapper.apolloClient.mutate(uploadFileMutation).toDeferred().await()
    }

    suspend fun sendFileResponse(
        id: String,
        key: String,
        uri: Uri
    ): Response<SendChatFileResponseMutation.Data> {
        val mimeType = fileService.getMimeType(uri) ?: ""

        val input = ChatResponseFileInput(
            body = ChatResponseBodyFileInput(
                key = key,
                mimeType = mimeType
            ),
            globalId = id
        )

        val chatFileResponse = SendChatFileResponseMutation(input)

        return apolloClientWrapper.apolloClient.mutate(chatFileResponse).toDeferred().await()
    }

    suspend fun editLastResponse() =
        apolloClientWrapper.apolloClient.mutate(EditLastResponseMutation()).toDeferred().await()

    fun triggerFreeTextChat(): Flow<Response<TriggerFreeTextChatMutation.Data>> {
        val triggerFreeTextChatMutation = TriggerFreeTextChatMutation()

        return apolloClientWrapper.apolloClient.mutate(triggerFreeTextChatMutation).toFlow()
    }

    suspend fun searchGifs(query: String) =
        apolloClientWrapper.apolloClient.query(GifQuery(query)).toDeferred().await()
}
