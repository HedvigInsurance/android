package com.hedvig.app.feature.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.net.Uri
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

    private val disposables = CompositeDisposable()

    private var isSubscriptionAllowedToWrite = true
    private var isWaitingForParagraph = false

    fun subscribe() {
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
        disposables += chatRepository
            .fetchChatMessages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                postResponseValue(response)
                if (isFirstParagraph(response)) {
                    waitForParagraph(getFirstParagraphDelay(response))
                }
                isSubscriptionAllowedToWrite = true
            }, {
                Timber.e(it)
            })
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
        isSubscriptionAllowedToWrite = false
        disposables += chatRepository
            .sendChatMessage(getLastId(), message)
            .subscribe({ response ->
                if (response.data()?.isSendChatTextResponse == true) {
                    load()
                }
                sendMessageResponse.postValue(response.data()?.isSendChatTextResponse)
            }, { Timber.e(it) })
    }

    private fun respondWithFile(key: String, uri: Uri) {
        isSubscriptionAllowedToWrite = false
        disposables += chatRepository
            .sendFileResponse(getLastId(), key, uri)
            .subscribe({ response ->
                if (response.data()?.isSendChatFileResponse == true) {
                    load()
                }
            }, {
                Timber.e(it)
            })
    }

    fun respondWithSingleSelect(value: String) {
        isSubscriptionAllowedToWrite = false
        disposables += chatRepository
            .sendSingleSelect(getLastId(), value)
            .subscribe({ response ->
                if (response.data()?.isSendChatSingleSelectResponse == true) {
                    load()
                }
            }, {
                Timber.e(it)
            })
    }

    private fun getLastId(): String =
        messages.value?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.globalId
            ?: throw RuntimeException("Messages is not initialized!")

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun uploadClaim(path: String) {
        disposables += chatRepository
            .uploadClaim(getLastId(), path)
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }
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
            }, { Timber.e(it) })
    }
}

