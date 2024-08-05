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
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.data.chat.database.ConversationDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.ConversationInfo.Info
import com.hedvig.android.feature.chat.cbm.ConversationInfo.NoConversation
import com.hedvig.android.feature.chat.cbm.ConversationInfoStatus.Failed
import com.hedvig.android.feature.chat.cbm.ConversationInfoStatus.Initializing
import com.hedvig.android.feature.chat.cbm.ConversationInfoStatus.Loaded
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class CbmChatViewModel(
  conversationId: String,
  database: AppDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  conversationDao: ConversationDao,
  chatRepository: Provider<CbmChatRepository>,
  clock: Clock,
) : MoleculeViewModel<CbmChatEvent, CbmChatUiState>(
    CbmChatUiState.Initializing,
    CbmChatPresenter(
      Uuid.fromString(conversationId),
      database,
      chatDao,
      remoteKeyDao,
      conversationDao,
      chatRepository,
      clock,
    ),
  )

internal class CbmChatPresenter(
  private val conversationId: Uuid,
  private val database: AppDatabase,
  private val chatDao: ChatDao,
  private val remoteKeyDao: RemoteKeyDao,
  private val conversationDao: ConversationDao,
  private val chatRepository: Provider<CbmChatRepository>,
  private val clock: Clock,
) : MoleculePresenter<CbmChatEvent, CbmChatUiState> {
  @OptIn(ExperimentalPagingApi::class)
  @Composable
  override fun MoleculePresenterScope<CbmChatEvent>.present(lastState: CbmChatUiState): CbmChatUiState {
    var conversationInfoStatus by remember {
      mutableStateOf(
        if (lastState is CbmChatUiState.Loaded) {
          ConversationInfoStatus.Loaded(lastState.backendConversationInfo)
        } else {
          ConversationInfoStatus.Initializing
        },
      )
    }
    var conversationIdStatusLoadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(conversationIdStatusLoadIteration) {
      if (conversationInfoStatus is ConversationInfoStatus.Loaded && conversationIdStatusLoadIteration == 0) {
        return@LaunchedEffect
      }
      conversationInfoStatus = ConversationInfoStatus.Initializing
      chatRepository.provide().getConversationInfo(conversationId).collect { result ->
        result.fold(
          ifLeft = {
            conversationInfoStatus = ConversationInfoStatus.Failed
          },
          ifRight = { conversationInfo ->
            conversationInfoStatus = ConversationInfoStatus.Loaded(conversationInfo)
          },
        )
      }
    }

    val updatedConversationInfoStatus by rememberUpdatedState(conversationInfoStatus)
    CollectEvents { event ->
      val startConversationIfNecessary = suspend {
        val conversationInfoStatusValue = updatedConversationInfoStatus
        val conversationAlreadyStarted = when (conversationInfoStatusValue) {
          Failed -> false
          Initializing -> false
          is Loaded -> {
            when (conversationInfoStatusValue.conversationInfo) {
              NoConversation -> false
              is Info -> true
            }
          }
        }
        if (!conversationAlreadyStarted) {
          chatRepository.provide().createConversation(conversationId).onRight { backendConversationInfo ->
            conversationInfoStatus = ConversationInfoStatus.Loaded(backendConversationInfo)
          }
        }
      }
      when (event) {
        CbmChatEvent.RetryLoadingChat -> conversationIdStatusLoadIteration++
        is CbmChatEvent.SendTextMessage -> launch {
          startConversationIfNecessary()
          chatRepository.provide().sendText(conversationId, null, event.message)
        }

        is CbmChatEvent.SendPhotoMessage -> launch {
          startConversationIfNecessary()
          chatRepository.provide().sendPhoto(conversationId, null, event.uri)
        }

        is CbmChatEvent.SendMediaMessage -> launch {
          startConversationIfNecessary()
          chatRepository.provide().sendMedia(conversationId, null, event.uri)
        }

        is CbmChatEvent.RetrySendChatMessage -> launch {
          startConversationIfNecessary()
          chatRepository.provide().retrySendMessage(conversationId, event.messageId)
        }
      }
    }

    return when (val conversationIdStatusValue = conversationInfoStatus) {
      Initializing -> CbmChatUiState.Initializing
      Failed -> CbmChatUiState.Error
      is Loaded -> {
        presentLoadedChat(
          conversationIdStatusValue.conversationInfo,
          conversationId,
          database,
          chatDao,
          remoteKeyDao,
          conversationDao,
          chatRepository,
          clock,
        )
      }
    }
  }
}

@OptIn(ExperimentalPagingApi::class)
@Composable
private fun presentLoadedChat(
  backendConversationInfo: ConversationInfo,
  conversationId: Uuid,
  database: AppDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  conversationDao: ConversationDao,
  chatRepository: Provider<CbmChatRepository>,
  clock: Clock,
): CbmChatUiState.Loaded {
  val coroutineScope = rememberCoroutineScope()
  val latestMessage by remember(chatDao) {
    chatDao.latestMessage(conversationId).filterNotNull().map(ChatMessageEntity::toLatestChatMessage)
  }.collectAsState(null)
  val bannerText by remember(conversationId, chatRepository) {
    flow { emitAll(chatRepository.provide().bannerText(conversationId)) }
  }.collectAsState(null)
  val pagingDataFlow = remember(backendConversationInfo) {
    val remoteMediator =
      ChatRemoteMediator(conversationId, database, chatDao, remoteKeyDao, conversationDao, chatRepository, clock)
    Pager(
      config = PagingConfig(pageSize = 50, prefetchDistance = 50, jumpThreshold = 10),
      remoteMediator = remoteMediator,
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

  LaunchedEffect(backendConversationInfo, conversationId, lazyPagingItems) {
    if (backendConversationInfo is ConversationInfo.NoConversation) return@LaunchedEffect
    snapshotFlow { lazyPagingItems.itemCount }
      .map { it > 0 }
      .distinctUntilChanged()
      .collectLatest { poll ->
        if (poll) {
          chatRepository.provide().pollNewestMessages(conversationId).collect {
            logcat { "Polling error: $it" }
          }
        }
      }
  }
  return CbmChatUiState.Loaded(
    backendConversationInfo = backendConversationInfo,
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
    val backendConversationInfo: ConversationInfo,
    // The list of messages, ordered from the newest one to the oldest one
    val messages: LazyPagingItems<CbmUiChatMessage>,
    val latestMessage: LatestChatMessage?,
    val bannerText: BannerText?,
  ) : CbmChatUiState {
    val topAppBarText: TopAppBarText = when (backendConversationInfo) {
      NoConversation -> TopAppBarText.NewConversation
      is Info -> {
        when {
          backendConversationInfo.isLegacy -> TopAppBarText.Legacy
          else -> TopAppBarText.Text(backendConversationInfo.title, backendConversationInfo.createdAt)
        }
      }
    }

    data class LatestChatMessage(
      val id: Uuid,
      val sender: Sender,
    )

    sealed interface TopAppBarText {
      data object NewConversation : TopAppBarText

      data object Legacy : TopAppBarText

      data class Text(
        val title: String,
        val submittedAt: Instant?,
      ) : TopAppBarText
    }
  }
}

internal data class CbmUiChatMessage(
  val chatMessage: CbmChatMessage,
  val isLastDeliveredMessage: Boolean,
)

private sealed interface ConversationInfoStatus {
  data object Initializing : ConversationInfoStatus

  data object Failed : ConversationInfoStatus

  data class Loaded(
    val conversationInfo: ConversationInfo,
  ) : ConversationInfoStatus
}
