package com.hedvig.android.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import arrow.core.identity
import arrow.fx.coroutines.parMap
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.feature.chat.data.ChatMessage
import com.hedvig.android.feature.chat.ui.UiChatMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal sealed interface ChatEventNew {
  data object FetchMoreMessages : ChatEventNew

  data object DismissError : ChatEventNew

  data class SendTextMessage(val message: String) : ChatEventNew

  data class SendFileMessage(val file: File) : ChatEventNew
}

@Immutable
internal sealed interface ChatUiState {
  data object Initializing : ChatUiState

  // TODO remove demo mode from here, just fake the dependencies instead?
  data object DemoMode : ChatUiState

  data object DisabledByFeatureFlag : ChatUiState

  @Immutable
  data class Loaded(
    // The list of messages, ordered from the newest one to the oldest one
    val messages: ImmutableList<UiChatMessage>,
    val errorMessage: ErrorMessage?,
    val fetchMoreMessagesUiState: FetchMoreMessagesUiState,
  ) : ChatUiState {
    sealed interface FetchMoreMessagesUiState {
      object FailedToFetch : FetchMoreMessagesUiState

      object FetchingMore : FetchMoreMessagesUiState

      object NothingMoreToFetch : FetchMoreMessagesUiState

      object StillInitializing : FetchMoreMessagesUiState
    }
  }
}

internal class ChatPresenter(
  private val chatRepository: Provider<ChatRepositoryNew>,
  private val chatClosedTracker: ChatClosedEventStore,
  private val featureManager: FeatureManager,
  private val demoManager: DemoManager,
) : MoleculePresenter<ChatEventNew, ChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ChatEventNew>.present(lastState: ChatUiState): ChatUiState {
    // region early exits
    val isInDemoMode by produceState(false || lastState is ChatUiState.DemoMode) {
      demoManager.isDemoMode().collectLatest { isDemoMode ->
        value = isDemoMode
      }
    }
    if (isInDemoMode) {
      return ChatUiState.DemoMode
    }
    val isChatDisabled by produceState(false || lastState is ChatUiState.DisabledByFeatureFlag) {
      value = featureManager.isFeatureEnabled(Feature.DISABLE_CHAT)
    }
    if (isChatDisabled) {
      return ChatUiState.DisabledByFeatureFlag
    }
    // endregion

    val messages: SnapshotStateList<ChatMessage> = remember { mutableStateListOf() }

    // False if messages from cache were loaded or if the initial network request has finished
    var isStillInitializing by remember { mutableStateOf(lastState is ChatUiState.Initializing) }

    var fetchMoreState by remember { mutableStateOf<FetchMoreState>(FetchMoreState.Idle) }
    var fetchMoreMessagesFetchIndex by remember { mutableIntStateOf(0) }
    var failedToFetchMoreMessages by remember { mutableStateOf(false) }

    val messagesToSend = remember { Channel<String>(Channel.UNLIMITED) }
    var messagesCurrentlyBeingSent: SnapshotStateList<String> = remember { mutableStateListOf() }
    var messagesFailedToBeSent: SnapshotStateList<String> = remember { mutableStateListOf() }

    LaunchMessagesWatcher(
      onCachedMessagesReceived = { cachedMessages ->
        isStillInitializing = false
        Snapshot.withMutableSnapshot {
          messages.clear()
          messages.addAll(cachedMessages)
        }
      },
    )
    LaunchPeriodicMessagePolls(
      isChatDisabled = isChatDisabled,
      getFetchMoreState = { fetchMoreState },
      setFetchMoreState = { fetchMoreState = it },
      onFinishedInitializing = { isStillInitializing = false },
    )
    LaunchFetchMoreMessagesEffect(
      fetchMoreMessagesFetchIndex = fetchMoreMessagesFetchIndex,
      fetchMoreState = fetchMoreState,
      setFetchMoreState = { fetchMoreState = it },
      failedToFetchMoreMessages = { failedToFetchMoreMessages = true },
    )
    LaunchNewMessageSendingEffect(
      messagesToSend = messagesToSend,
      addMessageCurrentlyBeingSent = { message -> messagesCurrentlyBeingSent.add(message) },
      removeMessageCurrentlyBeingSent = { message -> messagesCurrentlyBeingSent.remove(message) },
      reportMessageFailedToBeSent = { message ->
        Snapshot.withMutableSnapshot {
          messagesCurrentlyBeingSent.remove(message)
          messagesFailedToBeSent.add(message)
        }
      },
    )

    CollectEvents { event ->
      when (event) {
        ChatEventNew.DismissError -> TODO()
        ChatEventNew.FetchMoreMessages -> {
          if (failedToFetchMoreMessages) {
            Snapshot.withMutableSnapshot {
              failedToFetchMoreMessages = false
              fetchMoreMessagesFetchIndex++
            }
            return@CollectEvents
          }
          val fetchMoreStateValue = fetchMoreState
          if (fetchMoreStateValue !is FetchMoreState.IdleWithKnownNextFetch) return@CollectEvents
          fetchMoreState = FetchMoreState.FetchUntil(fetchMoreStateValue.fetchUntil)
        }

        is ChatEventNew.SendFileMessage -> TODO()
        is ChatEventNew.SendTextMessage -> {
          messagesToSend.trySend(event.message)
        }
      }
    }

    LaunchedEffect(messagesCurrentlyBeingSent.toList()) {
      messagesCurrentlyBeingSent.toList().also {
        logcat { "Stelios: messagesCurrentlyBeingSent:${messagesCurrentlyBeingSent.toList()}" }
      }
    }
    LaunchedEffect(messagesFailedToBeSent.toList()) {
      messagesFailedToBeSent.toList().also {
        logcat { "Stelios: messagesFailedToBeSent:${messagesFailedToBeSent.toList()}" }
      }
    }

    return if (isStillInitializing) {
      ChatUiState.Initializing
    } else {
      val uiChatMessagesBeingSent = messagesCurrentlyBeingSent.mapIndexed { index, text ->
        UiChatMessage(
          ChatMessage.ChatMessageText(
            id = Uuid.randomUUID().toString(),
            text = text,
            sender = ChatMessage.Sender.MEMBER,
            sentAt = Clock.System.now() - index.milliseconds,
          ),
          sentStatus = UiChatMessage.SentStatus.NotYetSent,
        )
      }
      val uiChatMessagesFailedToBeSent = messagesFailedToBeSent.mapIndexed { index, text ->
        UiChatMessage(
          ChatMessage.ChatMessageText(
            id = Uuid.randomUUID().toString(),
            text = text,
            sender = ChatMessage.Sender.MEMBER,
            sentAt = Clock.System.now() - index.milliseconds,
          ),
          sentStatus = UiChatMessage.SentStatus.FailedToBeSent,
        )
      }
      val sentUiChatMessages = messages.map { UiChatMessage(it, UiChatMessage.SentStatus.Sent) }

      val fetchMoreMessagesUiState = run {
        if (failedToFetchMoreMessages) return@run ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch
        when (fetchMoreState) {
          is FetchMoreState.Idle -> ChatUiState.Loaded.FetchMoreMessagesUiState.StillInitializing
          is FetchMoreState.FetchUntil -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.IdleWithKnownNextFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.NothingMoreToFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.NothingMoreToFetch
        }
      }
      ChatUiState.Loaded(
        messages = (sentUiChatMessages + uiChatMessagesBeingSent + uiChatMessagesFailedToBeSent)
          .sortedByDescending { it.chatMessage.sentAt }
          .toPersistentList(),
        errorMessage = null,
        fetchMoreMessagesUiState = fetchMoreMessagesUiState,
      )
    }
  }

  @Composable
  private fun LaunchPeriodicMessagePolls(
    isChatDisabled: Boolean,
    getFetchMoreState: () -> FetchMoreState,
    setFetchMoreState: (FetchMoreState) -> Unit,
    onFinishedInitializing: () -> Unit,
  ) {
    LaunchedEffect(isChatDisabled) {
      if (isChatDisabled) return@LaunchedEffect
      while (isActive) {
        chatRepository.provide().pollNewestMessages().onRight { result ->
          val fetchMoreStateValue = getFetchMoreState()
          if (fetchMoreStateValue is FetchMoreState.Idle) {
            setFetchMoreState(FetchMoreState.IdleWithKnownNextFetch(result.nextUntil))
          }
        }
        onFinishedInitializing()
        delay(5.seconds) // todo uncomment this
      }
    }
  }

  @Composable
  private fun LaunchMessagesWatcher(onCachedMessagesReceived: (List<ChatMessage>) -> Unit) {
    LaunchedEffect(Unit) {
      chatRepository.provide().watchMessages()
        .map {
          it.fold(
            // todo consider errors here? Or just ignore if cache fails for whatever reason?
            ifLeft = { emptyList() },
            ifRight = ::identity,
          )
        }
        .filter { it.isNotEmpty() }
        .collect { cachedMessages ->
          onCachedMessagesReceived(cachedMessages)
        }
    }
  }

  @Composable
  private fun LaunchFetchMoreMessagesEffect(
    fetchMoreMessagesFetchIndex: Int,
    fetchMoreState: FetchMoreState,
    setFetchMoreState: (FetchMoreState) -> Unit,
    failedToFetchMoreMessages: () -> Unit,
  ) {
    LaunchedEffect(fetchMoreMessagesFetchIndex, fetchMoreState) {
      val fetchUntil = fetchMoreState as? FetchMoreState.FetchUntil ?: return@LaunchedEffect
      chatRepository.provide()
        .fetchMoreMessages(fetchUntil.fetchUntil)
        .fold(
          ifLeft = {
            logcat { "Chat failed to fetch more messages:$it" }
            failedToFetchMoreMessages()
          },
          ifRight = { chatMessagesResult ->
            if (!chatMessagesResult.hasNext) {
              logcat { "Chat has fetched new data, but has no more messages to fetch, reached the end" }
              setFetchMoreState(FetchMoreState.NothingMoreToFetch)
            } else {
              logcat { "Chat has fetched new data, and even more exist, next until:${chatMessagesResult.nextUntil}" }
              setFetchMoreState(FetchMoreState.IdleWithKnownNextFetch(chatMessagesResult.nextUntil))
            }
          },
        )
    }
  }

  @Composable
  private fun LaunchNewMessageSendingEffect(
    messagesToSend: Channel<String>,
    addMessageCurrentlyBeingSent: (String) -> Unit,
    removeMessageCurrentlyBeingSent: (String) -> Unit,
    reportMessageFailedToBeSent: (String) -> Unit,
  ) {
    LaunchedEffect(messagesToSend) {
      messagesToSend.consumeAsFlow().parMap { message: String ->
        addMessageCurrentlyBeingSent(message)
        chatRepository.provide().sendMessage(message)
          .fold(
            ifLeft = {
              logcat(LogPriority.ERROR) { "Stelios: Failed to send message:$message | $it" }
              reportMessageFailedToBeSent(message)
            },
            ifRight = { chatMessage ->
              removeMessageCurrentlyBeingSent(message)
            },
          )
      }.collect()
    }
  }
}

@Immutable
private sealed interface FetchMoreState {
  data object Idle : FetchMoreState

  data class IdleWithKnownNextFetch(val fetchUntil: Instant) : FetchMoreState

  data class FetchUntil(val fetchUntil: Instant) : FetchMoreState

  data object NothingMoreToFetch : FetchMoreState
}
