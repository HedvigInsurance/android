package com.hedvig.android.feature.chat.cbm

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.flatMap
import androidx.paging.map
import com.benasher44.uuid.Uuid
import com.hedvig.android.feature.chat.cbm.ConversationIdStatus.Failed
import com.hedvig.android.feature.chat.cbm.ConversationIdStatus.Initializing
import com.hedvig.android.feature.chat.cbm.ConversationIdStatus.Loaded
import com.hedvig.android.feature.chat.cbm.database.AppDatabase
import com.hedvig.android.feature.chat.cbm.database.ChatDao
import com.hedvig.android.feature.chat.cbm.database.ChatMessageEntity
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.Sender
import com.hedvig.android.feature.chat.cbm.model.toChatMessage
import com.hedvig.android.feature.chat.cbm.model.toLatestChatMessage
import com.hedvig.android.feature.chat.cbm.paging.ChatRemoteMediator
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class CbmChatViewModel(
  conversationId: String,
  database: AppDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  chatRepository: CbmChatRepository, // todo cbm: Make this a provider for demo mode
) : MoleculeViewModel<CbmChatEvent, CbmChatUiState>(
    CbmChatUiState.Initializing,
    CbmChatPresenter(Uuid.fromString(conversationId), database, chatDao, remoteKeyDao, chatRepository),
  )

internal class CbmChatPresenter(
  private val conversationId: Uuid,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val chatRepository: CbmChatRepository,
) : MoleculePresenter<CbmChatEvent, CbmChatUiState> {
  @OptIn(ExperimentalPagingApi::class)
  @Composable
  override fun MoleculePresenterScope<CbmChatEvent>.present(lastState: CbmChatUiState): CbmChatUiState {
    var conversationIdStatus by remember {
      mutableStateOf(
        if (lastState is CbmChatUiState.Loaded) {
          ConversationIdStatus.Loaded(lastState.backendConversationId)
        } else {
          ConversationIdStatus.Initializing
        },
      )
    }
    var conversationIdStatusLoadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(conversationIdStatusLoadIteration) {
      if (conversationIdStatus is ConversationIdStatus.Loaded && conversationIdStatusLoadIteration == 0) {
        return@LaunchedEffect
      }
      conversationIdStatus = ConversationIdStatus.Initializing
      chatRepository.getConversation(conversationId).fold(
        ifLeft = {
          conversationIdStatus = ConversationIdStatus.Failed
        },
        ifRight = { converstationId ->
          conversationIdStatus = ConversationIdStatus.Loaded(converstationId)
        },
      )
    }

    val updatedConversationIdStatus by rememberUpdatedState(conversationIdStatus)
    CollectEvents { event ->
      val startConversationIfNecessary = suspend {
        val conversationIdStatusValue = updatedConversationIdStatus
        val conversationAlreadyStarted =
          (conversationIdStatusValue as? ConversationIdStatus.Loaded)?.backendConversationId != null
        if (!conversationAlreadyStarted) {
          chatRepository.createConversation(conversationId).onRight { backendConversationId ->
            conversationIdStatus = ConversationIdStatus.Loaded(backendConversationId)
          }
        }
      }
      when (event) {
        CbmChatEvent.RetryLoadingChat -> conversationIdStatusLoadIteration++
        is CbmChatEvent.SendTextMessage -> launch {
          startConversationIfNecessary()
          chatRepository.sendText(conversationId, event.message)
        }

        is CbmChatEvent.SendPhotoMessage -> launch {
          startConversationIfNecessary()
          chatRepository.sendPhoto(conversationId, event.uri)
        }

        is CbmChatEvent.SendMediaMessage -> launch {
          startConversationIfNecessary()
          chatRepository.sendMedia(conversationId, event.uri)
        }

        is CbmChatEvent.RetrySendChatMessage -> launch {
          startConversationIfNecessary()
          chatRepository.retrySendMessage(conversationId, event.messageId)
        }
      }
    }

    return when (val conversationIdStatusValue = conversationIdStatus) {
      Initializing -> CbmChatUiState.Initializing
      Failed -> CbmChatUiState.Error
      is Loaded -> {
        logcat { "Stelios Loaded" }
        presentLoadedChat(
          conversationIdStatusValue.backendConversationId,
          conversationId,
          database,
          chatDao,
          remoteKeyDao,
          chatRepository,
        )
      }
    }
  }
}

@OptIn(ExperimentalPagingApi::class)
@Composable
private fun presentLoadedChat(
  backendConversationId: String?,
  conversationId: Uuid,
  database: AppDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  chatRepository: CbmChatRepository,
): CbmChatUiState.Loaded {
  val coroutineScope = rememberCoroutineScope()
  val latestMessage by remember(chatDao) {
    chatDao.latestMessage(conversationId).filterNotNull().map(ChatMessageEntity::toLatestChatMessage)
  }.collectAsState(null)
  val bannerText by remember(conversationId, chatRepository) {
    chatRepository.bannerText(conversationId)
  }.collectAsState(null)
  val pagingDataFlow = remember(backendConversationId) {
    Pager(
      config = PagingConfig(pageSize = 50, prefetchDistance = 50, jumpThreshold = 10),
      remoteMediator = ChatRemoteMediator(conversationId, database, chatDao, remoteKeyDao, chatRepository),
      pagingSourceFactory = { chatDao.messages(conversationId) },
    ).flow
      .map { value ->
        value.flatMap { listOfNotNull(it.toChatMessage()) }
      }.cachedIn(coroutineScope)
  }
  val pagingData = remember(pagingDataFlow, chatDao) {
    combine(pagingDataFlow, chatDao.lastDeliveredMessage(conversationId)) { pagingData, lastDeliveredMessageId ->
      pagingData.map { cbmChatMessage ->
        CbmUiChatMessage(
          cbmChatMessage,
          cbmChatMessage.sender == Sender.MEMBER && cbmChatMessage.id == lastDeliveredMessageId.toString(),
        )
      }
    }.cachedIn(coroutineScope)
  }
  val lazyPagingItems = pagingData.collectAsLazyPagingItems()

  LaunchedEffect(backendConversationId, conversationId, lazyPagingItems) {
    if (backendConversationId == null) return@LaunchedEffect
    snapshotFlow { lazyPagingItems.itemCount }
      .map { it > 0 }
      .distinctUntilChanged()
      .collectLatest { poll ->
        if (poll) {
          chatRepository.pollNewestMessages(conversationId).collect {
            logcat { "Polling error: $it" }
          }
        }
      }
  }
  return CbmChatUiState.Loaded(
    backendConversationId = backendConversationId,
    messages = lazyPagingItems,
    latestMessage = latestMessage,
    bannerText = bannerText,
  )
}

internal sealed interface CbmChatEvent {
  data object RetryLoadingChat : CbmChatEvent

  data class SendTextMessage(
    val message: String,
  ) : CbmChatEvent

  data class RetrySendChatMessage(
    val messageId: String,
  ) : CbmChatEvent

  data class SendPhotoMessage(
    val uri: Uri,
  ) : CbmChatEvent

  data class SendMediaMessage(
    val uri: Uri,
  ) : CbmChatEvent
}

internal sealed interface CbmChatUiState {
  data object Initializing : CbmChatUiState

  data object Error : CbmChatUiState

  @Immutable
  data class Loaded(
    val backendConversationId: String?,
    // The list of messages, ordered from the newest one to the oldest one
    val messages: LazyPagingItems<CbmUiChatMessage>,
    val latestMessage: LatestChatMessage?,
    val bannerText: BannerText?,
  ) : CbmChatUiState {
    data class LatestChatMessage(
      val id: Uuid,
      val sender: Sender,
    )
  }
}

internal data class CbmUiChatMessage(
  val chatMessage: CbmChatMessage,
  val isLastDeliveredMessage: Boolean,
)

private sealed interface ConversationIdStatus {
  data object Initializing : ConversationIdStatus

  data object Failed : ConversationIdStatus

  data class Loaded(
    val backendConversationId: String?,
  ) : ConversationIdStatus
}
