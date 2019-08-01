package com.hedvig.app.feature.chat

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.graphql.UploadFileMutation
import com.hedvig.app.util.LiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
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
        disposables += chatRepository.subscribeToChatMessages()
            .subscribe({ response ->
                response.data()?.message?.let {
                    if (isSubscriptionAllowedToWrite) {
                        chatRepository
                            .writeNewMessage(
                                it.fragments.chatMessageFragment
                            )
                    }
                }
            }, {
                Timber.e(it)
            }, {
                //TODO: handle in UI
                Timber.i("subscribeToChatMessages was completed")
            })
    }

    fun load() {
        isSubscriptionAllowedToWrite = false
        if (chatDisposable.size() > 0) {
            chatDisposable.clear()
        }
        chatDisposable += chatRepository
            .fetchChatMessages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.hasErrors()) {
                    retryLoad()
                    isSubscriptionAllowedToWrite = true
                    return@subscribe
                }
                postResponseValue(response)
                if (isFirstParagraph(response)) {
                    waitForParagraph(getFirstParagraphDelay(response))
                }
                isSubscriptionAllowedToWrite = true
            }, {
                retryLoad()
                isSubscriptionAllowedToWrite = true
                Timber.e(it)
            })
    }

    private fun retryLoad() {
        if (loadRetries < 5) {
            loadRetries += 1
            disposables += Observable
                .timer(loadRetries, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    load()
                }, { Timber.e(it) })
        } else {
            networkError.postValue(true)
        }
    }

    private fun isFirstParagraph(response: Response<ChatMessagesQuery.Data>) =
        response.data()?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.body?.type == "paragraph"

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
        disposables += chatRepository
            .uploadFile(uri)
            .subscribe({ data ->
                data.data()?.let {
                    respondWithFile(it.uploadFile.key, uri)
                }
                onNext(data)
            }, { Timber.e(it) })
    }

    fun uploadFileFromProvider(uri: Uri) {
        isSubscriptionAllowedToWrite = false
        isUploading.value = true
        disposables += chatRepository
            .uploadFileFromProvider(uri)
            .subscribe({ data ->
                data.data()?.let {
                    respondWithFile(it.uploadFile.key, uri)
                    uploadBottomSheetResponse.postValue(data.data())
                }
            }, { Timber.e(it) })
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
        disposables += chatRepository
            .sendChatMessage(getLastId(), message)
            .subscribe({ response ->
                isSendingMessage = false
                if (response.data()?.isSendChatTextResponse == true) {
                    load()
                }
                sendMessageResponse.postValue(response.data()?.isSendChatTextResponse)
            }, {
                isSendingMessage = false
                Timber.e(it)
            })
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
                if (response.data()?.isSendChatFileResponse == true) {
                    load()
                }
            }, {
                isSendingMessage = false
                Timber.e(it)
            })
    }

    fun respondWithSingleSelect(value: String) {
        if (isSendingMessage) {
            return
        }
        isSendingMessage = true
        isSubscriptionAllowedToWrite = false
        disposables += chatRepository
            .sendSingleSelect(getLastId(), value)
            .subscribe({ response ->
                isSendingMessage = false
                if (response.data()?.isSendChatSingleSelectResponse == true) {
                    load()
                }
            }, {
                isSendingMessage = false
                Timber.e(it)
            })
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
        disposables += chatRepository
            .uploadClaim(getLastId(), path)
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }
                load()
            }, { Timber.e(it) })
    }

    fun editLastResponse() {
        disposables += chatRepository
            .editLastResponse()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }
                load()
            }, { Timber.e(it) })
    }
}

