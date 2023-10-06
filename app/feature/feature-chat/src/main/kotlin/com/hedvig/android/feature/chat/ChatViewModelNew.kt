package com.hedvig.app.feature.chat.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatMessage
import com.hedvig.app.feature.chat.data.ChatRepositoryNew
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

internal class ChatViewModelNew(
  chatRepository: Provider<ChatRepositoryNew>,
  chatClosedTracker: ChatEventStore,
  featureManager: FeatureManager,
) : MoleculeViewModel<ChatEventNew, ChatUiState>(
  ChatUiState(),
  ChatPresenter(
    chatRepository = chatRepository,
    chatClosedTracker = chatClosedTracker,
    featureManager = featureManager,
  ),
) {
  init {
    emit(ChatEventNew.FetchMessages())
    viewModelScope.launch {
      while (true) {
        delay(5.seconds)
        emit(ChatEventNew.PollMessages)
      }
    }
  }
}

internal class ChatPresenter(
  private val chatRepository: Provider<ChatRepositoryNew>,
  private val chatClosedTracker: ChatEventStore,
  private val featureManager: FeatureManager,
) : MoleculePresenter<ChatEventNew, ChatUiState> {

  @Composable
  override fun MoleculePresenterScope<ChatEventNew>.present(lastState: ChatUiState): ChatUiState {
    var isLoadingChat by remember { mutableStateOf(lastState.isLoadingChat) }
    var isLoadingNewMessages by remember { mutableStateOf(lastState.isLoadingNewMessages) }
    var messages by remember { mutableStateOf(lastState.messages) }
    var isSendingMessage by remember { mutableStateOf(lastState.isSendingMessage) }
    var canFetchMoreMessages by remember { mutableStateOf(lastState.canFetchMoreMessages) }
    var canFetchUntil by remember { mutableStateOf(lastState.canFetchUntil) }
    var errorMessage by remember { mutableStateOf(lastState.errorMessage) }

    var fetchId by remember { mutableIntStateOf(0) }
    var fetchUntil by remember { mutableStateOf<Instant?>(null) }
    var fileUrlInputState by remember { mutableStateOf<String?>(null) }
    var messageInputState by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      when (event) {
        is ChatEventNew.FetchMessages -> fetchUntil = event.until
        ChatEventNew.PollMessages -> fetchId++
        is ChatEventNew.SendFileMessage -> fileUrlInputState = event.url
        is ChatEventNew.SendTextMessage -> messageInputState = event.message
        is ChatEventNew.DismissError -> errorMessage = null
      }
    }

    LaunchedEffect(fetchId, fetchUntil) {
      isLoadingNewMessages = fetchUntil != null
      chatRepository.provide()
        .fetchChatMessages(fetchUntil)
        .fold(
          ifLeft = {
            errorMessage = it.message
            isLoadingChat = false
            isLoadingNewMessages = false
          },
          ifRight = {
            messages = it.messages.toPersistentList()
            canFetchMoreMessages = it.hasNext
            canFetchUntil = it.nextUntil
            isLoadingChat = false
            isLoadingNewMessages = false
          },
        )
    }

    LaunchedEffect(fileUrlInputState) {
      val fileUrl = fileUrlInputState ?: return@LaunchedEffect
      isSendingMessage = true
      chatRepository.provide()
        .sendFile(fileUrl)
        .fold(
          ifLeft = {
            errorMessage = it.message
            isSendingMessage = false
          },
          ifRight = {
            it.message?.let(messages::add)
            isSendingMessage = false
          },
        )
    }

    LaunchedEffect(messageInputState) {
      val message = messageInputState ?: return@LaunchedEffect
      isSendingMessage = true
      chatRepository.provide()
        .sendMessage(message)
        .fold(
          ifLeft = {
            errorMessage = it.message
            isSendingMessage = false
          },
          ifRight = {
            it.message?.let(messages::add)
            isSendingMessage = false
          },
        )
    }

    return ChatUiState(
      isLoadingChat = isLoadingChat,
      isLoadingNewMessages = isLoadingNewMessages,
      isSendingMessage = isSendingMessage,
      messages = messages,
      canFetchMoreMessages = canFetchMoreMessages,
      errorMessage = errorMessage,
    )
  }
}

internal sealed interface ChatEventNew {
  data class FetchMessages(val until: Instant? = null) : ChatEventNew

  data object PollMessages : ChatEventNew

  data object DismissError : ChatEventNew

  data class SendTextMessage(val message: String) : ChatEventNew

  data class SendFileMessage(val url: String) : ChatEventNew
}

internal data class ChatUiState(
  val isLoadingChat: Boolean = true,
  val isLoadingNewMessages: Boolean = false,
  val isSendingMessage: Boolean = false,
  val messages: PersistentList<ChatMessage> = persistentListOf(),
  val canFetchMoreMessages: Boolean = false,
  val canFetchUntil: Instant? = null,
  val errorMessage: String? = null,
)
