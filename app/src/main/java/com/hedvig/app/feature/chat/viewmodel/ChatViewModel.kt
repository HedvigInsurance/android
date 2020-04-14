package com.hedvig.app.feature.chat.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.graphql.GifQuery
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.app.feature.chat.FileUploadOutcome
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.util.LiveEvent
import e
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ChatViewModel(
    private val chatRepository: ChatRepository
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
    private val chatDisposable = CompositeDisposable()

    private var isSubscriptionAllowedToWrite = true
    private var isWaitingForParagraph = false
    private var isSendingMessage = false
    private var loadRetries = 0L

    fun subscribe() {
        if (chatDisposable.size() > 0) {
            chatDisposable.dispose()
        }
        viewModelScope.launch {
            chatRepository
                .subscribeToChatMessages()
                .onEach { response ->
                    response.data()?.message?.let { message ->
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
        if (chatDisposable.size() > 0) {
            chatDisposable.clear()
        }
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
                .subscribe({
                    load()
                }, { e(it) })
        } else {
            networkError.postValue(true)
        }
    }

    private fun isFirstParagraph(response: Response<ChatMessagesQuery.Data>) =
        (response.data()?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.body?.asMessageBodyCore)?.type == "paragraph"

    private fun getFirstParagraphDelay(response: Response<ChatMessagesQuery.Data>) =
        response.data()?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.header?.pollingInterval?.toLong()
            ?: 0L

    private fun waitForParagraph(delay: Long) {
        if (isWaitingForParagraph)
            return

        isWaitingForParagraph = true
        disposables += Observable
            .timer(delay, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                load()
                isWaitingForParagraph = false
            }, {})
    }

    fun uploadFile(uri: Uri) {
        uploadFile(uri) { data ->
            fileUploadOutcome.postValue(FileUploadOutcome(uri, !data.hasErrors()))
        }
    }

    fun uploadTakenPicture(uri: Uri) {
        uploadFile(uri) { data ->
            takePictureUploadOutcome.postValue(FileUploadOutcome(uri, !data.hasErrors()))
        }
    }

    private fun uploadFile(uri: Uri, onNext: (Response<UploadFileMutation.Data>) -> Unit) {
        isSubscriptionAllowedToWrite = false
        isUploading.value = true
        viewModelScope.launch {
            chatRepository.uploadFile(uri)
                .onEach { response ->
                    response.data()?.uploadFile?.key?.let { respondWithFile(it, uri) }
                    onNext(response)
                }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    fun uploadFileFromProvider(uri: Uri) {
        isSubscriptionAllowedToWrite = false
        isUploading.value = true
        viewModelScope.launch {
            chatRepository.uploadFileFromProvider(uri)
                .onEach {
                    it.data()?.uploadFile?.key?.let { key -> respondWithFile(key, uri) }
                }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    private fun postResponseValue(response: Response<ChatMessagesQuery.Data>) {
        val data = response.data()
        messages.postValue(data)
    }

    fun respondToLastMessage(message: String) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            chatRepository
                .sendChatMessage(getLastId(), message)
                .onEach { response ->
                    isSendingMessage = false
                    if (response.data()?.sendChatTextResponse == true) {
                        load()
                    }
                    sendMessageResponse.postValue(response.data()?.sendChatTextResponse)
                }.catch {
                    isSendingMessage = false
                    e(it)
                }
                .launchIn(this)
        }
    }

    private fun respondWithFile(key: String, uri: Uri) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        disposables += chatRepository
            .sendFileResponse(getLastId(), key, uri)
            .subscribe({ response ->
                isSendingMessage = false
                if (response.data()?.sendChatFileResponse == true) {
                    load()
                }
            }, {
                isSendingMessage = false
                e(it)
            })
    }

    fun respondWithSingleSelect(value: String) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        viewModelScope.launch {
            chatRepository
                .sendSingleSelect(getLastId(), value)
                .onEach { response ->
                    isSendingMessage = false
                    if (response.data()?.sendChatSingleSelectResponse == true) {
                        load()
                    }
                }
                .catch {
                    isSendingMessage = false
                    e(it)
                }
                .launchIn(this)
        }
    }

    private fun getLastId(): String =
        messages.value?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.globalId
            ?: throw RuntimeException("Messages is not initialized!")

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        chatDisposable.clear()
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
        disposables += chatRepository
            .editLastResponse()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { response.errors().toString() }
                    return@subscribe
                }
                load()
            }, { e(it) })
    }

    fun searchGifs(query: String) {
        disposables += chatRepository
            .searchGifs(query)
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { response.errors().toString() }
                }
                gifs.postValue(response.data())
            }, { e(it) })
    }
}

