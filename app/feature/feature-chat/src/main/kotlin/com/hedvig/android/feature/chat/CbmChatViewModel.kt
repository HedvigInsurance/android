package com.hedvig.android.feature.chat

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import android.view.accessibility.AccessibilityManager
import android.provider.Settings
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.flatMap
import androidx.paging.map
import androidx.room.RoomDatabase
import arrow.core.Either
import com.benasher44.uuid.Uuid
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.ChatMessageEntity
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.feature.chat.ConversationInfoStatus.Failed
import com.hedvig.android.feature.chat.ConversationInfoStatus.Initializing
import com.hedvig.android.feature.chat.ConversationInfoStatus.Loaded
import com.hedvig.android.feature.chat.data.BannerText
import com.hedvig.android.feature.chat.data.CbmChatRepository
import com.hedvig.android.feature.chat.data.ConversationInfo
import com.hedvig.android.feature.chat.data.ConversationInfo.Info
import com.hedvig.android.feature.chat.data.ConversationInfo.NoConversation
import com.hedvig.android.feature.chat.data.MessageSendError
import com.hedvig.android.feature.chat.model.CbmChatMessage
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.model.toChatMessage
import com.hedvig.android.feature.chat.model.toLatestChatMessage
import com.hedvig.android.feature.chat.paging.ChatRemoteMediator
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class CbmChatViewModel(
  conversationId: String,
  database: RoomDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  chatRepository: Provider<CbmChatRepository>,
  featureManager: FeatureManager,
  clock: Clock,
  context: Context,
  coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + AndroidUiDispatcher.Main),
) : MoleculeViewModel<CbmChatEvent, CbmChatUiState>(
    initialState = CbmChatUiState.Initializing,
    presenter = CbmChatPresenter(
      conversationId = Uuid.fromString(conversationId),
      pagingData = cbmChatPresenterPagingData(
        conversationId = conversationId,
        database = database,
        chatDao = chatDao,
        remoteKeyDao = remoteKeyDao,
        chatRepository = chatRepository,
        clock = clock,
        scope = coroutineScope,

      ),
      chatDao = chatDao,
      chatRepository = chatRepository,
      featureManager = featureManager,
      context
    ),
    coroutineScope = coroutineScope,

  )

@OptIn(ExperimentalPagingApi::class)
private fun cbmChatPresenterPagingData(
  conversationId: String,
  database: RoomDatabase,
  chatDao: ChatDao,
  remoteKeyDao: RemoteKeyDao,
  chatRepository: Provider<CbmChatRepository>,
  clock: Clock,
  scope: CoroutineScope,
): Flow<PagingData<CbmUiChatMessage>> {
  val conversationId = Uuid.fromString(conversationId)
  val remoteMediator = ChatRemoteMediator(conversationId, database, chatDao, remoteKeyDao, chatRepository, clock)
  val pagingDataFlow = Pager(
    config = PagingConfig(pageSize = 50, prefetchDistance = 50, jumpThreshold = 10),
    remoteMediator = remoteMediator,
    pagingSourceFactory = { chatDao.messages(conversationId) },
  )
    .flow
    .map { value ->
      value.flatMap { listOfNotNull(it.toChatMessage()) }
    }.cachedIn(scope)
  return combine(pagingDataFlow, chatDao.lastDeliveredMessage(conversationId)) { pagingData, lastDeliveredMessageId ->
    pagingData.map { cbmChatMessage ->
      CbmUiChatMessage(
        cbmChatMessage,
        cbmChatMessage.sender == Sender.MEMBER && cbmChatMessage.id == lastDeliveredMessageId.toString(),
      )
    }
  }
}

internal class CbmChatPresenter(
  private val conversationId: Uuid,
  private val pagingData: Flow<PagingData<CbmUiChatMessage>>,
  private val chatDao: ChatDao,
  private val chatRepository: Provider<CbmChatRepository>,
  private val featureManager: FeatureManager,
  private val context: Context,
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
    val numberOfOngoingUploads = remember { MutableStateFlow<Int>(0) }
    var showFileTooBigErrorToast by remember { mutableStateOf(false) }
    var hideBanner by remember { mutableStateOf(false) }
    var showFileFailedToBeSendToast by remember { mutableStateOf(false) }
    val a11yOn = isAccessibilityEnabled(context)
    val enableInlineMediaPlayer by remember(featureManager ) {
      if (!a11yOn) {
        featureManager.isFeatureEnabled(Feature.ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES)
      } else {
        flowOf(false)
      }
    }.collectAsState(false)

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
          numberOfOngoingUploads.update { it + 1 }
          startConversationIfNecessary()
          chatRepository.provide().sendText(conversationId, null, event.message)
          numberOfOngoingUploads.update { it - 1 }
        }

        is CbmChatEvent.SendPhotoMessage -> launch {
          numberOfOngoingUploads.update { it + 1 }
          startConversationIfNecessary()
          chatRepository.provide().sendPhotos(conversationId, event.uriList)
          numberOfOngoingUploads.update { it - 1 }
        }

        is CbmChatEvent.SendMediaMessage -> launch {
          numberOfOngoingUploads.update { it + 1 }
          startConversationIfNecessary()
          val result = chatRepository.provide().sendMedia(conversationId, event.uriList)
          Snapshot.withMutableSnapshot {
            for (result in result) {
              result.onError(
                onFailedToPersistUriPermissionError = { showFileFailedToBeSendToast = true },
                onFileTooBigError = { showFileTooBigErrorToast = true },
              )
            }
          }
          numberOfOngoingUploads.update { it - 1 }
        }

        is CbmChatEvent.RetrySendChatMessage -> launch {
          numberOfOngoingUploads.update { it + 1 }
          startConversationIfNecessary()
          val result = chatRepository.provide().retrySendMessage(conversationId, event.messageId)
          result.onError(
            onFailedToPersistUriPermissionError = { showFileFailedToBeSendToast = true },
            onFileTooBigError = { showFileTooBigErrorToast = true },
          )
          numberOfOngoingUploads.update { it - 1 }
        }

        CbmChatEvent.ClearFileTooBigToast -> showFileTooBigErrorToast = false
        CbmChatEvent.ClearFileFailedToBeSentToast -> showFileFailedToBeSendToast = false
        CbmChatEvent.HideBanner -> {
          hideBanner = true
        }
      }
    }

    return when (val conversationIdStatusValue = conversationInfoStatus) {
      Initializing -> CbmChatUiState.Initializing
      Failed -> CbmChatUiState.Error
      is Loaded -> {
        presentLoadedChat(
          pagingData = pagingData,
          backendConversationInfo = conversationIdStatusValue.conversationInfo,
          enableInlineMediaPlayer = enableInlineMediaPlayer,
          conversationId = conversationId,
          chatDao = chatDao,
          chatRepository = chatRepository,
          showUploading = numberOfOngoingUploads.collectAsState().value > 0,
          showFileTooBigErrorToast = showFileTooBigErrorToast,
          hideBanner = hideBanner,
          showFileFailedToBeSendToast = showFileFailedToBeSendToast,
        )
      }
    }
  }
}

@OptIn(ExperimentalPagingApi::class)
@Composable
private fun presentLoadedChat(
  pagingData: Flow<PagingData<CbmUiChatMessage>>,
  backendConversationInfo: ConversationInfo,
  enableInlineMediaPlayer: Boolean,
  conversationId: Uuid,
  chatDao: ChatDao,
  chatRepository: Provider<CbmChatRepository>,
  showUploading: Boolean,
  showFileTooBigErrorToast: Boolean,
  hideBanner: Boolean,
  showFileFailedToBeSendToast: Boolean,
): CbmChatUiState.Loaded {
  val latestMessage by remember(chatDao) {
    chatDao.latestMessage(conversationId).filterNotNull().map(ChatMessageEntity::toLatestChatMessage)
  }.collectAsState(null)

  val bannerText by if (!hideBanner) {
    remember(conversationId, chatRepository) {
      flow { emitAll(chatRepository.provide().bannerText(conversationId)) }
    }.collectAsState(null)
  } else {
    remember { mutableStateOf(null) }
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
    enableInlineMediaPlayer = enableInlineMediaPlayer,
    showUploading = showUploading,
    showFileTooBigErrorToast = showFileTooBigErrorToast,
    showFileFailedToBeSentToast = showFileFailedToBeSendToast,
  )
}

internal sealed interface CbmChatEvent {
  data object RetryLoadingChat : CbmChatEvent

  data object HideBanner : CbmChatEvent

  data class SendTextMessage(
    val message: String,
  ) : CbmChatEvent

  data class RetrySendChatMessage(
    val messageId: String,
  ) : CbmChatEvent

  data class SendPhotoMessage(
    val uriList: List<Uri>,
  ) : CbmChatEvent

  data class SendMediaMessage(
    val uriList: List<Uri>,
  ) : CbmChatEvent

  data object ClearFileTooBigToast : CbmChatEvent

  data object ClearFileFailedToBeSentToast : CbmChatEvent
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
    val enableInlineMediaPlayer: Boolean,
    val showUploading: Boolean,
    val showFileTooBigErrorToast: Boolean,
    // When we fail to persist the message in a way where we can retry it later, we simply fall back to showing an error
    val showFileFailedToBeSentToast: Boolean,
  ) : CbmChatUiState {
    val topAppBarText: TopAppBarText = when (backendConversationInfo) {
      NoConversation -> TopAppBarText.NewConversation
      is Info -> {
        when {
          backendConversationInfo.isLegacy -> TopAppBarText.Legacy
          backendConversationInfo.claimInfo != null -> TopAppBarText.ClaimConversation(
            backendConversationInfo.claimInfo.claimType,
            backendConversationInfo.createdAt,
          )

          else -> TopAppBarText.ServiceConversation(backendConversationInfo.createdAt)
        }
      }
    }
    val claimId: String? = when (backendConversationInfo) {
      NoConversation -> null
      is Info -> backendConversationInfo.claimInfo?.claimId
    }

    data class LatestChatMessage(
      val id: Uuid,
      val sender: Sender,
    )

    sealed interface TopAppBarText {
      data object NewConversation : TopAppBarText

      data object Legacy : TopAppBarText

      data class ClaimConversation(val claimType: String?, val createdAt: Instant) : TopAppBarText

      data class ServiceConversation(val createdAt: Instant) : TopAppBarText
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

private fun Either<MessageSendError, *>.onError(
  onFailedToPersistUriPermissionError: (MessageSendError.FailedToPersistUriPermissionError) -> Unit,
  onFileTooBigError: (MessageSendError.FileTooBigError) -> Unit,
) {
  when (this) {
    is Either.Left<MessageSendError> -> when (val error = value) {
      is MessageSendError.FailedToPersistUriPermissionError -> onFailedToPersistUriPermissionError(error)
      is MessageSendError.FileTooBigError -> onFileTooBigError(error)
      is MessageSendError.GenericError -> {}
    }

    is Either.Right<*> -> {}
  }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
//  val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
//  val enabled = accessibilityManager.getEnabledAccessibilityServiceList()
  val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
  val accessibilityEnabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
  return accessibilityEnabled && enabledServices != null
}
