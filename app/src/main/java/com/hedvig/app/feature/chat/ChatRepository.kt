package com.hedvig.app.feature.chat

import android.content.Context
import android.net.Uri
import com.apollographql.apollo.api.FileUpload
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.graphql.*
import com.hedvig.android.owldroid.type.*
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.graphql.EditLastResponseMutation
import com.hedvig.android.owldroid.graphql.SendChatSingleSelectResponseMutation
import com.hedvig.android.owldroid.graphql.SendChatTextResponseMutation
import com.hedvig.android.owldroid.graphql.UploadClaimMutation
import com.hedvig.android.owldroid.type.ChatResponseBodySingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseBodyTextInput
import com.hedvig.android.owldroid.type.ChatResponseSingleSelectInput
import com.hedvig.android.owldroid.type.ChatResponseTextInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.service.FileService
import com.hedvig.app.util.extensions.into
import io.reactivex.Observable
import java.io.File

class ChatRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val fileService: FileService,
    private val context: Context
) {
    private lateinit var messagesQuery: ChatMessagesQuery

    fun fetchChatMessages(): Observable<Response<ChatMessagesQuery.Data>> {
        messagesQuery = ChatMessagesQuery()
        return Rx2Apollo.from(
            apolloClientWrapper.apolloClient
                .query(messagesQuery)
                .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
                .watcher()
        )
    }

    fun subscribeToChatMessages() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.subscribe(ChatMessageSubscription.builder().build()))

    fun sendChatMessage(id: String, message: String): Observable<Response<SendChatTextResponseMutation.Data>> {
        val input = ChatResponseTextInput.builder()
            .globalId(id)
            .body(ChatResponseBodyTextInput.builder().text(message).build())
            .build()

        val sendChatMessageMutation =
            SendChatTextResponseMutation.builder()
                .input(input)
                .build()

        return Rx2Apollo.from(
            apolloClientWrapper.apolloClient.mutate(sendChatMessageMutation)
        )
    }

    fun sendSingleSelect(id: String, value: String): Observable<Response<SendChatSingleSelectResponseMutation.Data>> {
        val input = ChatResponseSingleSelectInput.builder()
            .globalId(id)
            .body(
                ChatResponseBodySingleSelectInput
                    .builder()
                    .selectedValue(value)
                    .build()
            )
            .build()

        val sendChatSingleSelectMutation = SendChatSingleSelectResponseMutation
            .builder()
            .input(input)
            .build()

        return Rx2Apollo.from(
            apolloClientWrapper.apolloClient.mutate(sendChatSingleSelectMutation)
        )
    }

    fun uploadClaim(id: String, path: String): Observable<Response<UploadClaimMutation.Data>> {
        val mutation = UploadClaimMutation.builder()
            .id(id)
            .claim(FileUpload(fileService.getMimeType(path), File(path)))
            .build()

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(mutation))
    }

    fun writeNewMessage(message: ChatMessageFragment) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(messagesQuery)
            .execute()

        val chatMessagesFragment =
            ChatMessagesQuery
                .Message
                .Fragments.builder().chatMessageFragment(message).build()

        val chatMessageQueryBuilder =
            ChatMessagesQuery
                .Message
                .builder()
                .__typename(message.__typename)
                .fragments(
                    chatMessagesFragment
                )

        val newMessagesBuilder = cachedData
            .toBuilder()
            .messages { it.add(0, chatMessageQueryBuilder) }

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(messagesQuery, newMessagesBuilder.build())
            .execute()
    }

    fun uploadFileFromProvider(uri: Uri): Observable<Response<UploadFileMutation.Data>> {
        val file = File.createTempFile(TEMP_FILE_PREFIX, null) // I hate this but it seems there's no other way
        context.contentResolver.openInputStream(uri)?.into(file)
        return uploadFile(file, fileService.getMimeType(uri) ?: "")
    }

    fun uploadFile(uri: Uri): Observable<Response<UploadFileMutation.Data>> =
        uploadFile(File(uri.path), fileService.getMimeType(uri) ?: "")

    private fun uploadFile(file: File, mimeType: String): Observable<Response<UploadFileMutation.Data>> {
        val uploadFileMutation = UploadFileMutation
            .builder()
            .file(FileUpload(mimeType, file))
            .build()

        return Rx2Apollo.from(
            apolloClientWrapper.apolloClient.mutate(uploadFileMutation))
    }

    fun sendFileResponse(id: String, key: String, uri: Uri): Observable<Response<SendChatFileResponseMutation.Data>> {
        val mimeType = fileService.getMimeType(uri) ?: ""

        val input = ChatResponseFileInput
            .builder()
            .body(
                ChatResponseBodyFileInput
                    .builder()
                    .key(key)
                    .mimeType(mimeType)
                    .build()
            )
            .globalId(id)
            .build()

        val chatFileResponse = SendChatFileResponseMutation.builder()
            .input(input)
            .build()

        return Rx2Apollo.from(
            apolloClientWrapper.apolloClient.mutate(chatFileResponse))
    }

    fun editLastResponse() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(EditLastResponseMutation()))

    fun triggerFreeTextChat(): Observable<Response<TriggerFreeTextChatMutation.Data>> {
        val triggerFreeTextChatMutation = TriggerFreeTextChatMutation.builder().build()

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(triggerFreeTextChatMutation))
    }

    companion object {
        const val TEMP_FILE_PREFIX = "hedvig_upload_temp_file"
    }
}
