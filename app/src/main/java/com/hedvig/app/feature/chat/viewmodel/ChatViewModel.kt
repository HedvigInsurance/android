package com.hedvig.app.feature.chat.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.graphql.GifQuery
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.chat.FileUploadOutcome
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.hanalytics.HAnalytics
import e
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authenticationTokenService: AuthenticationTokenService,
    private val apolloClient: ApolloClient,
    private val chatClosedTracker: ChatEventStore,
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    val messages = MutableLiveData<ChatMessagesQuery.Data>()
    val sendMessageResponse = MutableLiveData<Boolean>()
    val isUploading = LiveEvent<Boolean>()
    val uploadBottomSheetResponse = LiveEvent<UploadFileMutation.Data>()
    val fileUploadOutcome = LiveEvent<FileUploadOutcome>()
    val takePictureUploadOutcome = LiveEvent<FileUploadOutcome>()
    val networkError = LiveEvent<Boolean>()
    val gifs = MutableLiveData<GifQuery.Data>()

    private val disposables = CompositeDisposable()

    private var isSubscriptionAllowedToWrite = true
    private var isWaitingForParagraph = false
    private var isSendingMessage = false
    private var loadRetries = 0L

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        object Restart : Event()
        object Error : Event()
    }

    fun subscribe() {
        viewModelScope.launch {
            chatRepository
                .subscribeToChatMessages()
                .onEach { response ->
                    response.data?.message?.let { message ->
                        if (isSubscriptionAllowedToWrite) {
                            chatRepository
                                .writeNewMessage(
                                    message.fragments.chatMessageFragment
                                )
                        }
                    }
                }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    fun load() {
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            chatRepository
                .fetchChatMessages()
                .onEach { response ->
                    postResponseValue(response)
                    if (isFirstParagraph(response)) {
                        waitForParagraph(getFirstParagraphDelay(response))
                    }
                    isSubscriptionAllowedToWrite = true
                }.catch {
                    retryLoad()
                    isSubscriptionAllowedToWrite = true
                    e(it)
                }
                .launchIn(this)
        }
    }

    private fun retryLoad() {
        if (loadRetries < 5) {
            loadRetries += 1
            disposables += Observable
                .timer(loadRetries, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        load()
                    },
                    { e(it) }
                )
        } else {
            networkError.postValue(true)
        }
    }

    private fun isFirstParagraph(response: Response<ChatMessagesQuery.Data>) = response
        .data
        ?.messages
        ?.firstOrNull()
        ?.fragments
        ?.chatMessageFragment
        ?.body
        ?.asMessageBodyCore
        ?.type == "paragraph"

    private fun getFirstParagraphDelay(response: Response<ChatMessagesQuery.Data>) =
        response.data?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.header?.pollingInterval?.toLong()
            ?: 0L

    private fun waitForParagraph(delay: Long) {
        if (isWaitingForParagraph)
            return

        isWaitingForParagraph = true
        disposables += Observable
            .timer(delay, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    load()
                    isWaitingForParagraph = false
                },
                {}
            )
    }

    fun uploadFile(uri: Uri) {
        hAnalytics.chatRichMessageSent()
        uploadFile(uri) { data ->
            fileUploadOutcome.postValue(FileUploadOutcome(uri, !data.hasErrors()))
        }
    }

    fun uploadTakenPicture(uri: Uri) {
        hAnalytics.chatRichMessageSent()
        uploadFile(uri) { data ->
            takePictureUploadOutcome.postValue(FileUploadOutcome(uri, !data.hasErrors()))
        }
    }

    private fun uploadFile(uri: Uri, onNext: (Response<UploadFileMutation.Data>) -> Unit) {
        isSubscriptionAllowedToWrite = false
        isUploading.value = true
        viewModelScope.launch {
            val response = runCatching { chatRepository.uploadFile(uri) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.uploadFile?.key?.let { respondWithFile(it, uri) }
            response.getOrNull()?.let { onNext(it) }
        }
    }

    fun uploadFileFromProvider(uri: Uri) {
        hAnalytics.chatRichMessageSent()
        isSubscriptionAllowedToWrite = false
        isUploading.value = true
        viewModelScope.launch {
            val response = runCatching { chatRepository.uploadFileFromProvider(uri) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.uploadFile?.key?.let { key ->
                respondWithFile(
                    key,
                    uri
                )
            }
            response.getOrNull()?.data?.let(uploadBottomSheetResponse::postValue)
        }
    }

    private fun postResponseValue(response: Response<ChatMessagesQuery.Data>) {
        response.data?.let { messages.postValue(it) }
    }

    fun respondWithGif(url: String) {
        hAnalytics.chatRichMessageSent()
        respondToLastMessage(url)
    }

    fun respondToLastMessage(message: String) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            val response = runCatching {
                chatRepository.sendChatMessage(getLastId(), message)
            }
            if (response.isFailure) {
                isSendingMessage = false
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            isSendingMessage = false
            if (response.getOrNull()?.data?.sendChatTextResponse == true) {
                load()
            }
            sendMessageResponse.postValue(response.getOrNull()?.data?.sendChatTextResponse)
        }
    }

    private fun respondWithFile(key: String, uri: Uri) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            val response = runCatching {
                chatRepository
                    .sendFileResponse(getLastId(), key, uri)
            }
            if (response.isFailure) {
                isSendingMessage = false
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            isSendingMessage = false
            if (response.getOrNull()?.data?.sendChatFileResponse == true) {
                load()
            }
        }
    }

    fun respondWithSingleSelect(value: String) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            val response = runCatching {
                chatRepository
                    .sendSingleSelect(getLastId(), value)
            }
            if (response.isFailure) {
                isSendingMessage = false
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            isSendingMessage = false
            if (response.getOrNull()?.data?.sendChatSingleSelectResponse == true) {
                load()
            }
        }
    }

    private fun getLastId(): String =
        messages.value?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.globalId
            ?: throw RuntimeException("Messages is not initialized!")

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun uploadClaim(path: String) {
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            val response = runCatching { chatRepository.uploadClaim(getLastId(), path) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            load()
        }
    }

    fun editLastResponse() {
        viewModelScope.launch {
            val response = runCatching { chatRepository.editLastResponse() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            load()
        }
    }

    fun searchGifs(query: String) {
        viewModelScope.launch {
            val response = runCatching { chatRepository.searchGifs(query) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { gifs.postValue(it) }
        }
    }

    fun restartChat() {
        viewModelScope.launch {
            authenticationTokenService.authenticationToken = null
            val response = runCatching { userRepository.logout() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "$it Failed to log out" } }
                _events.trySend(Event.Error)
            }
            apolloClient.subscriptionManager.reconnect()
            _events.trySend(Event.Restart)
        }
    }

    fun onChatClosed() {
        viewModelScope.launch {
            chatClosedTracker.increaseChatClosedCounter()
        }
    }
}
