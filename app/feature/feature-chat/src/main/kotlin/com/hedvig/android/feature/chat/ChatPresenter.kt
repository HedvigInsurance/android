package com.hedvig.android.feature.chat

import android.net.Uri
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
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.feature.chat.data.ChatRepository
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal sealed interface ChatEvent {
  data object FetchMoreMessages : ChatEvent

  data object DismissError : ChatEvent

  data class SendTextMessage(val message: String) : ChatEvent

  data class SendPhotoMessage(val uri: Uri) : ChatEvent

  data class SendMediaMessage(val uri: Uri) : ChatEvent
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
    val messages: ImmutableList<ChatMessage>,
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
  private val chatRepository: Provider<ChatRepository>,
  private val chatClosedTracker: ChatClosedEventStore,
  private val featureManager: FeatureManager,
  private val demoManager: DemoManager,
  private val clock: Clock,
) : MoleculePresenter<ChatEvent, ChatUiState> {
  @Composable
  override fun MoleculePresenterScope<ChatEvent>.present(lastState: ChatUiState): ChatUiState {
    // region early exits
    val isInDemoMode by produceState(lastState is ChatUiState.DemoMode) {
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

    val messages: SnapshotStateList<ChatMessage> = remember {
      mutableStateListOf(
        *(lastState.safeCast<ChatUiState.Loaded>()?.messages ?: emptyList()).toTypedArray(),
      )
    }

    // False if messages from cache were loaded or if the initial network request has finished
    var isStillInitializing by remember { mutableStateOf(lastState is ChatUiState.Initializing) }

    var fetchMoreState by remember { mutableStateOf<FetchMoreState>(FetchMoreState.Idle) }
    var fetchMoreMessagesFetchIndex by remember { mutableIntStateOf(0) }
    var failedToFetchMoreMessages by remember { mutableStateOf(false) }

    // Maybe merge the two text/uri queues together with a common type instead, to respect ordering them properly
    val photosToSend = remember { Channel<Uri>(Channel.UNLIMITED) }
    var photosFailedToBeSent: SnapshotStateList<FailedMessage.FailedUri> = remember { mutableStateListOf() }
    val mediaToSend = remember { Channel<Uri>(Channel.UNLIMITED) }
    var mediaFailedToBeSent: SnapshotStateList<FailedMessage.FailedUri> = remember { mutableStateListOf() }
    val messagesToSend = remember { Channel<String>(Channel.UNLIMITED) }
    var messagesFailedToBeSent: SnapshotStateList<FailedMessage.FailedText> = remember { mutableStateListOf() }

    LaunchMessagesWatcher(
      onCachedMessagesReceived = { cachedMessages ->

        Snapshot.withMutableSnapshot {
          isStillInitializing = false
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
    LaunchNewPhotoSendingEffect(
      photosToSend = photosToSend,
      reportPhotoFailedToBeSent = { uri ->
        photosFailedToBeSent.add(FailedMessage.FailedUri(uri, clock.now()))
      },
    )
    LaunchNewMediaSendingEffect(
      mediaToSend = mediaToSend,
      reportMediaFailedToBeSent = { uri ->
        mediaFailedToBeSent.add(FailedMessage.FailedUri(uri, clock.now()))
      },
    )
    LaunchNewMessageSendingEffect(
      messagesToSend = messagesToSend,
      reportMessageFailedToBeSent = { message ->
        messagesFailedToBeSent.add(FailedMessage.FailedText(message, clock.now()))
      },
    )

    CollectEvents { event ->
      logcat { "ChatPresenter handling event:$event" }
      when (event) {
        ChatEvent.DismissError -> TODO()
        ChatEvent.FetchMoreMessages -> {
          if (failedToFetchMoreMessages) {
            Snapshot.withMutableSnapshot {
              failedToFetchMoreMessages = false
              fetchMoreMessagesFetchIndex++
            }
          } else {
            val fetchMoreStateValue = fetchMoreState
            if (fetchMoreStateValue is FetchMoreState.IdleWithKnownNextFetch) {
              fetchMoreState = FetchMoreState.FetchUntil(fetchMoreStateValue.fetchUntil)
            }
          }
        }

        is ChatEvent.SendPhotoMessage -> {
          logcat { "Sending photo ${event.uri.path}" }
          photosToSend.trySend(event.uri).also {
            logcat { "Stelios photosToSend.trySend result:$it" }
          }
        }

        is ChatEvent.SendMediaMessage -> {
          logcat { "Sending media ${event.uri.path}" }
          mediaToSend.trySend(event.uri).also {
            logcat { "Stelios mediaToSend.trySend result:$it" }
          }
        }

        is ChatEvent.SendTextMessage -> {
          messagesToSend.trySend(event.message)
        }
      }
    }

    // todo remove this for production
    LaunchedEffect(messagesFailedToBeSent.toList()) {
      messagesFailedToBeSent.toList().also {
        logcat { "Stelios: messagesFailedToBeSent:${messagesFailedToBeSent.toList()}" }
      }
    }

    // todo remove this for production
    LaunchedEffect(photosFailedToBeSent.toList()) {
      photosFailedToBeSent.toList().also {
        logcat { "Stelios: filesFailedToBeSent:${photosFailedToBeSent.toList()}" }
      }
    }

    return if (isStillInitializing) {
      ChatUiState.Initializing
    } else {
      val fetchMoreMessagesUiState = run {
        if (failedToFetchMoreMessages) return@run ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch
        when (fetchMoreState) {
          is FetchMoreState.Idle -> ChatUiState.Loaded.FetchMoreMessagesUiState.StillInitializing
          is FetchMoreState.FetchUntil -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.IdleWithKnownNextFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
          is FetchMoreState.NothingMoreToFetch -> ChatUiState.Loaded.FetchMoreMessagesUiState.NothingMoreToFetch
        }
      }
      val failedUiChatMessages = (messagesFailedToBeSent + photosFailedToBeSent + mediaFailedToBeSent).map {
        it.toChatMessage()
      }
      ChatUiState.Loaded(
        messages = (messages + failedUiChatMessages)
          .sortedByDescending(ChatMessage::sentAt)
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
  private fun LaunchNewPhotoSendingEffect(photosToSend: Channel<Uri>, reportPhotoFailedToBeSent: (Uri) -> Unit) {
    LaunchedEffect(photosToSend) {
      photosToSend.receiveAsFlow().parMap { uri: Uri ->
        logcat { "Handling sending photo with uri:${uri.path}" }
        chatRepository.provide().sendPhoto(uri).onLeft {
          logcat(LogPriority.WARN) { "Failed to send photo:${uri.path} | $it" }
          reportPhotoFailedToBeSent(uri)
        }
      }.collect()
    }
  }

  @Composable
  private fun LaunchNewMediaSendingEffect(mediaToSend: Channel<Uri>, reportMediaFailedToBeSent: (Uri) -> Unit) {
    LaunchedEffect(mediaToSend) {
      mediaToSend.receiveAsFlow().parMap { uri: Uri ->
        logcat { "Handling sending media with uri:${uri.path}" }
        chatRepository.provide().sendMedia(uri).onLeft {
          logcat(LogPriority.WARN) { "Failed to send media:${uri.path} | $it" }
          reportMediaFailedToBeSent(uri)
        }
      }.collect()
    }
  }

  @Composable
  private fun LaunchNewMessageSendingEffect(
    messagesToSend: Channel<String>,
    reportMessageFailedToBeSent: (String) -> Unit,
  ) {
    LaunchedEffect(messagesToSend) {
      messagesToSend.consumeAsFlow().parMap { message: String ->
        logcat { "Handling sending message with text:$message" }
        chatRepository.provide().sendMessage(message).onLeft {
          logcat(LogPriority.WARN) { "Failed to send message:$it" }
          reportMessageFailedToBeSent(message)
        }
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

private sealed interface FailedMessage {
  val sentAt: Instant

  data class FailedText(val message: String, override val sentAt: Instant) : FailedMessage

  data class FailedUri(val uri: Uri, override val sentAt: Instant) : FailedMessage
}

private fun FailedMessage.toChatMessage(): ChatMessage {
  return when (this) {
    is FailedMessage.FailedText -> {
      ChatMessage.FailedToBeSent.ChatMessageText(
        id = Uuid.randomUUID().toString(),
        sentAt = this.sentAt,
        text = this.message,
      )
    }

    is FailedMessage.FailedUri -> {
      ChatMessage.FailedToBeSent.ChatMessageUri(
        id = Uuid.randomUUID().toString(),
        sentAt = this.sentAt,
        uri = this.uri,
      )
    }
  }
}
