package com.hedvig.app.feature.chat.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import giraffe.ChatMessagesQuery
import giraffe.GifQuery
import giraffe.UploadFileMutation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ChatViewModel(
  private val chatRepository: ChatRepository,
  private val chatClosedTracker: ChatEventStore,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    hAnalytics.screenView(AppScreen.CHAT)
  }

  val messages = MutableLiveData<ChatMessagesQuery.Data>()
  val sendMessageResponse = MutableLiveData<Boolean>()
  val isUploading = LiveEvent<Boolean>()
  val uploadBottomSheetResponse = LiveEvent<UploadFileMutation.Data>()
  val takePictureUploadFinished = LiveEvent<Unit>() // Reports that the picture upload was done, even if it failed
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
    object Error : Event()
  }

  fun subscribe() {
    viewModelScope.launch {
      chatRepository
        .subscribeToChatMessages()
        .onStart {
          logcat { "Chat: start subscription" }
        }
        .onEach { response ->
          logcat { "Chat: subscription response null?:${response.data == null}" }
          response.data?.message?.let { message ->
            if (isSubscriptionAllowedToWrite) {
              chatRepository
                .writeNewMessage(
                  message.fragments.chatMessageFragment,
                )
            } else {
              logcat(LogPriority.INFO) { "Chat: subscription was not allowed to write" }
            }
          }
        }
        .catch { logcat(LogPriority.ERROR, it) { "Chat: Error on chat subscription" } }
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
          logcat(LogPriority.ERROR, it) { "fetch chat messages response threw an exception" }
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
          { logcat(LogPriority.ERROR, it) { "retry load threw an exception" } },
        )
    } else {
      networkError.postValue(true)
    }
  }

  private fun isFirstParagraph(response: ApolloResponse<ChatMessagesQuery.Data>) = response
    .data
    ?.messages
    ?.firstOrNull()
    ?.fragments
    ?.chatMessageFragment
    ?.body
    ?.asMessageBodyCore
    ?.type == "paragraph"

  private fun getFirstParagraphDelay(response: ApolloResponse<ChatMessagesQuery.Data>) =
    response.data?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.header?.pollingInterval?.toLong()
      ?: 0L

  private fun waitForParagraph(delay: Long) {
    if (isWaitingForParagraph) {
      return
    }

    isWaitingForParagraph = true
    disposables += Observable
      .timer(delay, TimeUnit.MILLISECONDS, Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        {
          load()
          isWaitingForParagraph = false
        },
        {},
      )
  }

  fun uploadTakenPicture(uri: Uri) {
    hAnalytics.chatRichMessageSent()
    viewModelScope.launch {
      uploadFileInner(uri)
      takePictureUploadFinished.postValue(Unit)
    }
  }

  private suspend fun uploadFileInner(uri: Uri): UploadFileMutation.Data? {
    isSubscriptionAllowedToWrite = false
    isUploading.value = true
    val response = chatRepository.uploadFile(uri)
    return response.fold(
      ifLeft = {
        _events.send(Event.Error)
        null
      },
      ifRight = { data ->
        respondWithFile(data.uploadFile.key, uri)
        return data
      },
    )
  }

  fun uploadFileFromProvider(uri: Uri) {
    hAnalytics.chatRichMessageSent()
    isSubscriptionAllowedToWrite = false
    isUploading.value = true
    viewModelScope.launch {
      val response = chatRepository.uploadFileFromProvider(uri)
      response.fold(
        ifLeft = {
          _events.send(Event.Error)
        },
        ifRight = { data ->
          respondWithFile(
            key = data.uploadFile.key,
            uri = uri,
          )
          uploadBottomSheetResponse.postValue(data)
        },
      )
    }
  }

  private fun postResponseValue(response: ApolloResponse<ChatMessagesQuery.Data>) {
    response.data?.let { messages.postValue(it) }
  }

  fun respondWithGif(url: String) {
    hAnalytics.chatRichMessageSent()
    respondToLastMessage(url)
  }

  fun respondWithTextMessage(message: String) {
    hAnalytics.chatTextMessageSent()
    respondToLastMessage(message)
  }

  private fun respondToLastMessage(message: String) {
    if (isSendingMessage) {
      return
    }
    isSendingMessage = true
    isSubscriptionAllowedToWrite = false
    viewModelScope.launch {
      val response = chatRepository.sendChatMessage(getLastId(), message)
      isSendingMessage = false
      response.fold(
        ifLeft = {
          _events.send(Event.Error)
        },
        ifRight = { data ->
          if (data.sendChatTextResponse) {
            load()
          }
          sendMessageResponse.postValue(data.sendChatTextResponse)
        },
      )
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
        response.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "sendFileResponse failed" } }
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
        response.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "sendSingleSelect failed" } }
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
      ?: error("Messages is not initialized!")

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }

  fun searchGifs(query: String) {
    viewModelScope.launch {
      val response = runCatching { chatRepository.searchGifs(query) }
      if (response.isFailure) {
        response.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "searchGifs thew an exception" } }
        return@launch
      }
      response.getOrNull()?.data?.let { gifs.postValue(it) }
    }
  }

  fun onChatClosed() {
    viewModelScope.launch {
      chatClosedTracker.increaseChatClosedCounter()
    }
  }
}
