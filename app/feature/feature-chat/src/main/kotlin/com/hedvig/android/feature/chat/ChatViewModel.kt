package com.hedvig.android.feature.chat

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.chat.legacy.LiveEvent
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import giraffe.ChatMessagesQuery
import giraffe.GifQuery
import giraffe.UploadFileMutation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class ChatViewModel(
  private val chatRepository: ChatRepository,
  private val chatClosedTracker: ChatEventStore,
  private val featureManager: FeatureManager,
  private val demoManager: DemoManager,
) : ViewModel() {

  private val retryChannel = RetryChannel()

  private val _messages = MutableStateFlow<ChatMessagesQuery.Data?>(null)
  val messages = _messages.asStateFlow()

  val chatEnabledStatus: StateFlow<ChatEnabledStatus> = flow {
    val isInDemoMode = demoManager.isDemoMode().first()
    if (isInDemoMode) {
      emit(ChatEnabledStatus.Disabled.IsInDemoMode)
      return@flow
    }
    val chatIsDisabledByFeatureFlag = featureManager.isFeatureEnabled(Feature.DISABLE_CHAT)
    if (chatIsDisabledByFeatureFlag) {
      emit(ChatEnabledStatus.Disabled.FromFeatureFlag)
      return@flow
    }
    emit(ChatEnabledStatus.Enabled)
  }
    .onEach {
      logcat { "chatEnabledStatus:$it" }
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      ChatEnabledStatus.Uninitialized,
    )

  init {
    viewModelScope.launch {
      logcat(LogPriority.VERBOSE) { "Chat: fetchChatMessages starting" }
      retryChannel
        .flatMapLatest {
          chatRepository
            .fetchChatMessages()
            .catch {
              logcat(LogPriority.ERROR, it) { "chatRepository.fetchChatMessages threw an exception" }
            }
        }
        .collect { response ->
          logcat(LogPriority.VERBOSE) {
            "Chat: new response from chat query with #${response.data?.messages?.count() ?: 0} messages"
          }
          response.data?.let { responseData -> _messages.update { responseData } }
        }
      logcat(LogPriority.VERBOSE) { "Chat: fetchChatMessages finished" }
    }
    viewModelScope.launch {
      // Wait with loading the messages until we know the chat should be functioning
      chatEnabledStatus.first { it is ChatEnabledStatus.Enabled }
      logcat(LogPriority.VERBOSE) { "Chat: subscribeToChatMessages starting" }
      retryChannel
        .flatMapLatest {
          chatRepository.subscribeToChatMessages()
            .onStart { logcat { "Chat: start subscription" } }
            .catch {
              logcat(throwable = it) { "Chat: Error on chat subscription" }
              _events.send(ChatEvent.RetryableNonDismissibleNetworkError)
            }
        }
        .collect { response ->
          logcat { "Chat: subscription response null?:${response.data == null}" }
          // Write to cache
          response.data?.message?.fragments?.chatMessageFragment?.let {
            chatRepository.writeNewMessageToApolloCache(it)
          }
        }
      logcat(LogPriority.VERBOSE) { "Chat: subscribeToChatMessages finished" }
    }
  }

  val isUploading = LiveEvent<Boolean>()
  val uploadBottomSheetResponse = LiveEvent<UploadFileMutation.Data>()
  val takePictureUploadFinished = LiveEvent<Unit>() // Reports that the picture upload was done, even if it failed
  val gifs = MutableLiveData<GifQuery.Data>()

  private var isSendingMessage = false

  private val _events = Channel<ChatEvent>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  fun retry() {
    logcat { "Chat: retrying" }
    retryChannel.retry()
  }

  fun uploadTakenPicture(uri: Uri) {
    viewModelScope.launch {
      uploadFileInner(uri)
      takePictureUploadFinished.postValue(Unit)
    }
  }

  private suspend fun uploadFileInner(uri: Uri): UploadFileMutation.Data? {
    isUploading.value = true
    val response = chatRepository.uploadFile(uri)
    return response.fold(
      ifLeft = {
        _events.send(ChatEvent.Error)
        null
      },
      ifRight = { data ->
        respondWithFile(data.uploadFile.key, uri)
        return data
      },
    )
  }

  fun uploadFileFromProvider(uri: Uri) {
    isUploading.value = true
    viewModelScope.launch {
      val response = chatRepository.uploadFileFromProvider(uri)
      response.fold(
        ifLeft = {
          _events.send(ChatEvent.Error)
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

  fun respondWithGif(url: String) {
    respondToLastMessage(url)
  }

  fun respondWithTextMessage(message: String) {
    respondToLastMessage(message)
  }

  private fun respondToLastMessage(message: String) {
    if (isSendingMessage) {
      return
    }
    isSendingMessage = true
    viewModelScope.launch {
      val response = chatRepository.sendChatMessage(getLastId(), message)
      isSendingMessage = false
      response.fold(
        ifLeft = {
          _events.send(ChatEvent.Error)
        },
        ifRight = {
          _events.send(ChatEvent.ClearTextFieldInput)
        },
      )
    }
  }

  private fun respondWithFile(key: String, uri: Uri) {
    if (isSendingMessage) {
      return
    }
    isSendingMessage = true
    viewModelScope.launch {
      val response = runCatching {
        chatRepository.sendFileResponse(getLastId(), key, uri)
      }
      isSendingMessage = false
      if (response.isFailure) {
        response.exceptionOrNull()?.let {
          logcat(LogPriority.ERROR, it) { "sendFileResponse failed" }
        }
        return@launch
      }
    }
  }

  fun respondWithSingleSelect(value: String) {
    if (isSendingMessage) {
      return
    }
    isSendingMessage = true
    viewModelScope.launch {
      val response = runCatching {
        chatRepository
          .sendSingleSelect(getLastId(), value)
      }
      isSendingMessage = false
      if (response.isFailure) {
        response.exceptionOrNull()?.let { logcat(LogPriority.ERROR, it) { "sendSingleSelect failed" } }
        return@launch
      }
    }
  }

  private fun getLastId(): String =
    _messages.value?.messages?.firstOrNull()?.fragments?.chatMessageFragment?.globalId
      ?: error("Messages is not initialized!")

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

sealed class ChatEvent {
  // A generic error message, which results in a dismissible dialog which only lets members contact via email
  object Error : ChatEvent()

  // A dialog which should let the member retry loading the messages, else exit the chat if they don't wnat to retry.
  object RetryableNonDismissibleNetworkError : ChatEvent()

  // An event to inform the UI that the current input field can be cleared. Used after a message was successfully sent
  object ClearTextFieldInput : ChatEvent()
}

sealed interface ChatEnabledStatus {
  data object Uninitialized : ChatEnabledStatus
  data object Enabled : ChatEnabledStatus
  sealed interface Disabled : ChatEnabledStatus {
    data object FromFeatureFlag : Disabled
    data object IsInDemoMode : Disabled
  }
}
