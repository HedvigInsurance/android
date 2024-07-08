package com.hedvig.android.feature.chat.cbm

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.hedvig.android.feature.chat.cbm.database.AppDatabase
import com.hedvig.android.feature.chat.cbm.database.ChatDao
import com.hedvig.android.feature.chat.cbm.database.ChatMessageEntity
import com.hedvig.android.feature.chat.cbm.database.RemoteKeyDao
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.Sender
import com.hedvig.android.feature.chat.cbm.model.toChatMessage
import com.hedvig.android.feature.chat.cbm.model.toLatestChatMessage
import com.hedvig.android.feature.chat.cbm.paging.ChatRemoteMediator
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
    val coroutineScope = rememberCoroutineScope()
    val latestMessage by remember(chatDao) {
      chatDao.latestMessage(conversationId).filterNotNull().map(ChatMessageEntity::toLatestChatMessage)
    }.collectAsState(null)
    val bannerText by remember(conversationId, chatRepository) {
      chatRepository.bannerText(conversationId)
    }.collectAsState(null)
    val pagingDataFlow = remember {
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
            cbmChatMessage.id == lastDeliveredMessageId.toString(),
          )
        }
      }.cachedIn(coroutineScope)
    }
    val lazyPagingItems = pagingData.collectAsLazyPagingItems()

    LaunchedEffect(conversationId, lazyPagingItems) {
      snapshotFlow { lazyPagingItems.itemCount }
        .map { it > 0 }
        .distinctUntilChanged()
        .collectLatest { poll ->
          if (poll) {
            chatRepository.pollNewestMessages(conversationId).collect {
              println("Polling error: $it")
            }
          }
        }
    }
    CollectEvents { event ->
      when (event) {
        is CbmChatEvent.SendTextMessage -> launch {
          chatRepository.sendText(conversationId, event.message)
        }

        is CbmChatEvent.SendPhotoMessage -> launch {
          chatRepository.sendPhoto(conversationId, event.uri)
        }

        is CbmChatEvent.SendMediaMessage -> launch {
          chatRepository.sendMedia(conversationId, event.uri)
        }

        is CbmChatEvent.RetrySendChatMessage -> launch {
          chatRepository.retrySendMessage(conversationId, event.messageId)
        }
      }
    }
    return CbmChatUiState.Loaded(
      lazyPagingItems,
      latestMessage,
      bannerText,
    )
  }
}

internal sealed interface CbmChatEvent {
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

  @Immutable
  data class Loaded(
    // The list of messages, ordered from the newest one to the oldest one
    val messages: LazyPagingItems<CbmUiChatMessage>,
    val latestMessage: LatestChatMessage?,
    val bannerText: String?,
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
