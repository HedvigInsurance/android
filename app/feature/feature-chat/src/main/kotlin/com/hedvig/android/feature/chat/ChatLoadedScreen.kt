package com.hedvig.android.feature.chat

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorConfig
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.NullRequestDataException
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.MultipleDocuments
import com.hedvig.android.design.system.hedvig.icon.Refresh
import com.hedvig.android.design.system.hedvig.placeholder.fade
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.rememberPreviewSimpleCache
import com.hedvig.android.design.system.hedvig.rememberShapedColorPainter
import com.hedvig.android.design.system.hedvig.videoplayer.Media
import com.hedvig.android.design.system.hedvig.videoplayer.MediaState
import com.hedvig.android.design.system.hedvig.videoplayer.ResizeMode
import com.hedvig.android.design.system.hedvig.videoplayer.ShowBuffering
import com.hedvig.android.design.system.hedvig.videoplayer.SimpleVideoController
import com.hedvig.android.design.system.hedvig.videoplayer.SurfaceType
import com.hedvig.android.design.system.hedvig.videoplayer.rememberControllerState
import com.hedvig.android.design.system.hedvig.videoplayer.rememberMediaState
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.LatestChatMessage
import com.hedvig.android.feature.chat.data.BannerText
import com.hedvig.android.feature.chat.data.BannerText.ClosedConversation
import com.hedvig.android.feature.chat.data.ConversationInfo.Info
import com.hedvig.android.feature.chat.data.ConversationInfo.Info.ClaimInfo
import com.hedvig.android.feature.chat.model.CbmChatMessage
import com.hedvig.android.feature.chat.model.CbmChatMessage.ChatMessageFile
import com.hedvig.android.feature.chat.model.CbmChatMessage.ChatMessageFile.MimeType.IMAGE
import com.hedvig.android.feature.chat.model.CbmChatMessage.ChatMessageGif
import com.hedvig.android.feature.chat.model.CbmChatMessage.FailedToBeSent.ChatMessageMedia
import com.hedvig.android.feature.chat.model.CbmChatMessage.FailedToBeSent.ChatMessagePhoto
import com.hedvig.android.feature.chat.model.CbmChatMessage.FailedToBeSent.ChatMessageText
import com.hedvig.android.feature.chat.model.Sender
import com.hedvig.android.feature.chat.model.Sender.HEDVIG
import com.hedvig.android.feature.chat.model.Sender.MEMBER
import com.hedvig.android.feature.chat.ui.ChatBanner
import com.hedvig.android.feature.chat.ui.ChatInput
import com.hedvig.android.feature.chat.ui.TextWithClickableUrls
import com.hedvig.android.feature.chat.ui.adjustSizeToImageRatio
import com.hedvig.android.feature.chat.ui.backgroundColor
import com.hedvig.android.feature.chat.ui.formattedDateTime
import com.hedvig.android.feature.chat.ui.messageHorizontalAlignment
import com.hedvig.android.feature.chat.ui.onBackgroundColor
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun CbmChatLoadedScreen(
  uiState: Loaded,
  imageLoader: ImageLoader,
  simpleVideoCache: Cache,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (List<Uri>) -> Unit,
  onSendMedia: (List<Uri>) -> Unit,
) {
  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current
  val onMessageSent = {
    focusManager.clearFocus()
    coroutineScope.launch { lazyListState.scrollToItem(0) }
  }
  ChatLoadedScreen(
    uiState = uiState,
    lazyListState = lazyListState,
    imageLoader = imageLoader,
    simpleVideoCache = simpleVideoCache,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    onRetrySendChatMessage = onRetrySendChatMessage,
    chatInput = {
      ChatInput(
        onSendMessage = {
          onSendMessage(it)
          onMessageSent()
        },
        onSendPhoto = {
          onSendPhoto(it)
          onMessageSent()
        },
        onSendMedia = {
          onSendMedia(it)
          onMessageSent()
        },
        appPackageId = appPackageId,
        modifier = Modifier.padding(16.dp),
        showUploading = uiState.showUploading,
      )
    },
  )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatLoadedScreen(
  uiState: Loaded,
  lazyListState: LazyListState,
  imageLoader: ImageLoader,
  simpleVideoCache: Cache,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  chatInput: @Composable () -> Unit,
) {
  val dividerColor = HedvigTheme.colorScheme.borderSecondary
  val dividerThickness = 1.dp
  SelectionContainer {
    Column {
      ChatLazyColumn(
        lazyListState = lazyListState,
        messages = uiState.messages,
        latestChatMessage = uiState.latestMessage,
        imageLoader = imageLoader,
        simpleVideoCache = simpleVideoCache,
        openUrl = openUrl,
        onNavigateToImageViewer = onNavigateToImageViewer,
        onRetrySendChatMessage = onRetrySendChatMessage,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clearFocusOnTap(),
      )
      if (uiState.bannerText != null) {
        AnimatedVisibility(
          visible = WindowInsets.imeAnimationTarget.asPaddingValues().calculateBottomPadding() == 0.dp,
          enter = expandVertically(
            spring(
              stiffness = Spring.StiffnessMedium,
              visibilityThreshold = IntSize.VisibilityThreshold,
            ),
          ),
          exit = shrinkVertically(
            spring(
              stiffness = Spring.StiffnessMedium,
              visibilityThreshold = IntSize.VisibilityThreshold,
            ),
          ),
        ) {
          ChatBanner(
            text = when (uiState.bannerText) {
              ClosedConversation -> stringResource(R.string.CHAT_CONVERSATION_CLOSED_INFO)
              is BannerText.Text -> uiState.bannerText.text
            },
            modifier = Modifier
              .fillMaxWidth()
              .drawWithContent {
                drawContent()
                drawLine(
                  color = dividerColor,
                  strokeWidth = dividerThickness.toPx(),
                  start = Offset(0f, dividerThickness.toPx() / 2),
                  end = Offset(size.width, dividerThickness.toPx() / 2),
                )
              },
          )
        }
      }
      Box(
        propagateMinConstraints = true,
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
          .drawWithContent {
            drawContent()
            drawLine(
              color = dividerColor,
              strokeWidth = dividerThickness.toPx(),
              start = Offset(0f, dividerThickness.toPx() / 2),
              end = Offset(size.width, dividerThickness.toPx() / 2),
            )
          },
      ) {
        chatInput()
      }
    }
  }
}

@Composable
private fun ScrollToBottomEffect(
  lazyListState: LazyListState,
  latestChatMessage: LatestChatMessage?,
  messages: LazyPagingItems<CbmUiChatMessage>,
) {
  val updatedLatestChatMessage by rememberUpdatedState(latestChatMessage)
  LaunchedEffect(lazyListState) {
    snapshotFlow { updatedLatestChatMessage }
      .filterNotNull()
      .distinctUntilChangedBy(LatestChatMessage::id)
      .collectLatest { chatMessage ->
        val idToScrollTo = chatMessage.id
        withTimeout(1.seconds) {
          snapshotFlow {
            messages.itemSnapshotList
              .getOrNull(0)
              ?.chatMessage
              ?.id
          }.filterNotNull()
            .first { it == idToScrollTo.toString() }
        }
        ensureActive()
        val senderIsMember = chatMessage.sender == Sender.MEMBER
        val isAlreadyCloseToTheBottom = lazyListState.firstVisibleItemIndex <= 2
        if (senderIsMember || isAlreadyCloseToTheBottom) {
          lazyListState.scrollToItem(0)
        }
      }
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ChatLazyColumn(
  lazyListState: LazyListState,
  messages: LazyPagingItems<CbmUiChatMessage>,
  latestChatMessage: LatestChatMessage?,
  imageLoader: ImageLoader,
  simpleVideoCache: Cache,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val mediaStatesWithPlayers = remember { mutableStateMapOf<String, MediaState>() }
  DisposableEffect(
    Unit,
  ) {
    onDispose {
      mediaStatesWithPlayers.values.forEach { it.player?.release() }
      mediaStatesWithPlayers.clear() // todo: sure?
    }
  }
  ScrollToBottomEffect(
    lazyListState = lazyListState,
    latestChatMessage = latestChatMessage,
    messages = messages,
  )
  val appendStatus by remember(messages) {
    derivedStateOf { messages.loadState.append }
  }
  LazyColumn(
    state = lazyListState,
    reverseLayout = true,
    contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues(),
    modifier = modifier,
  ) {
    items(
      count = messages.itemCount,
      key = messages.itemKey { it.chatMessage.id },
      contentType = messages.itemContentType { uiChatMessage ->
        when (uiChatMessage.chatMessage) {
          is ChatMessageFile -> {
            when (uiChatMessage.chatMessage.mimeType) {
              ChatMessageFile.MimeType.IMAGE -> "ChatMessage.ChatMessageFileImage"
              ChatMessageFile.MimeType.MP4 -> "ChatMessage.ChatMessageFileMP4"
              ChatMessageFile.MimeType.PDF -> "ChatMessage.ChatMessageFilePDF"
              ChatMessageFile.MimeType.OTHER -> "ChatMessage.ChatMessageFileOther"
            }
          }

          is ChatMessageGif -> "ChatMessage.ChatMessageGif"
          is CbmChatMessage.ChatMessageText -> "ChatMessage.ChatMessageText"
          is ChatMessageText -> "ChatMessage.FailedToBeSent.ChatMessageText"
          is ChatMessagePhoto -> "ChatMessage.FailedToBeSent.ChatMessagePhoto"
          is ChatMessageMedia -> "ChatMessage.FailedToBeSent.ChatMessageMedia"
        }
      },
    ) { index: Int ->
      val uiChatMessage = messages[index]
      val alignment: Alignment.Horizontal = uiChatMessage?.chatMessage.messageHorizontalAlignment(index)
      val defaultWidth = 0.8f
      var dynamicBubbleWidthFraction by remember { mutableFloatStateOf(defaultWidth) }
      ChatBubble(
        uiChatMessage = uiChatMessage,
        chatItemIndex = index,
        imageLoader = imageLoader,
        simpleVideoCache = simpleVideoCache,
        mediaStatesWithPlayersMap = mediaStatesWithPlayers,
        openUrl = openUrl,
        onNavigateToImageViewer = onNavigateToImageViewer,
        onRetrySendChatMessage = onRetrySendChatMessage,
        onGoFullWidth = {
          dynamicBubbleWidthFraction = 1f
        },
        onGoDefaultWidth = {
          dynamicBubbleWidthFraction = defaultWidth
        },
        showingFullWidth = dynamicBubbleWidthFraction != defaultWidth,
        modifier = Modifier
          .fillParentMaxWidth()
          .padding(horizontal = 16.dp)
          .wrapContentWidth(alignment)
          .fillParentMaxWidth(dynamicBubbleWidthFraction)
          .wrapContentWidth(alignment)
          .padding(bottom = 8.dp),
      )
    }
    if (appendStatus !is LoadState.NotLoading) {
      item(
        key = "fetching_more",
        contentType = "fetching_more",
      ) {
        LaunchedEffect(Unit) {
          while (isActive) {
            if (appendStatus is LoadState.Error) {
              messages.retry()
            }
            delay(5.seconds)
          }
        }
        val hideForFirstFrames by produceState(true) {
          delay(200)
          value = false
        }
        ThreeDotsLoading(
          Modifier
            .fillParentMaxWidth()
            .wrapContentWidth()
            .padding(24.dp)
            .then(
              if (hideForFirstFrames) {
                Modifier.withoutPlacement()
              } else {
                Modifier
              },
            ),
        )
      }
    }
  }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ChatBubble(
  uiChatMessage: CbmUiChatMessage?,
  chatItemIndex: Int,
  imageLoader: ImageLoader,
  simpleVideoCache: Cache,
  mediaStatesWithPlayersMap: SnapshotStateMap<String, MediaState>,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier = Modifier,
) {
  val chatMessage = uiChatMessage?.chatMessage
  ChatMessageWithTimeAndDeliveryStatus(
    messageSlot = {
      when (chatMessage) {
        null -> {
          Box(
            Modifier.hedvigPlaceholder(
              true,
              shape = HedvigTheme.shapes.cornerLarge,
              highlight = PlaceholderHighlight.shimmer(),
            ),
          ) {
            HedvigText(
              text = "HHHHHHHHHH",
              modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .withoutPlacement(),
            )
          }
        }

        is CbmChatMessage.ChatMessageText -> {
          Surface(
            shape = HedvigTheme.shapes.cornerLarge,
            color = chatMessage.backgroundColor(),
            contentColor = chatMessage.onBackgroundColor(),
          ) {
            TextWithClickableUrls(
              text = chatMessage.text,
              onUrlClicked = openUrl,
              style = LocalTextStyle.current.copy(color = LocalContentColor.current),
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
          }
        }

        is ChatMessageFile -> {
          when (chatMessage.mimeType) {
            CbmChatMessage.ChatMessageFile.MimeType.IMAGE -> {
              ChatAsyncImage(
                model = chatMessage.url,
                imageLoader = imageLoader,
                cacheKey = chatMessage.id,
                modifier = Modifier.clickable {
                  onNavigateToImageViewer(chatMessage.url, chatMessage.id)
                },
              )
            }
            ChatMessageFile.MimeType.MP4 -> {
              val mediaState = mediaStatesWithPlayersMap.getOrPut(
                chatMessage.id,
                {
                  videoPlayerMediaState(simpleVideoCache, chatMessage.url)
                },
              )
              VideoMessage(
                state = mediaState,
                onGoFullWidth = onGoFullWidth,
                onGoDefaultWidth = onGoDefaultWidth,
                showingFullWidth = showingFullWidth,
              )
            }

            ChatMessageFile.MimeType.PDF, // todo chat: consider rendering PDFs inline in the chat
            ChatMessageFile.MimeType.OTHER,
            -> {
              AttachedFileMessage(onClick = { openUrl(chatMessage.url) })
            }
          }
        }

        is CbmChatMessage.ChatMessageGif -> {
          ChatAsyncImage(model = chatMessage.gifUrl, imageLoader = imageLoader, cacheKey = chatMessage.gifUrl)
        }

        is CbmChatMessage.FailedToBeSent -> {
          when (chatMessage) {
            is ChatMessageText -> {
              Surface(
                shape = HedvigTheme.shapes.cornerLarge,
                color = HedvigTheme.colorScheme.signalRedFill,
                contentColor = HedvigTheme.colorScheme.signalRedText,
                onClick = {
                  onRetrySendChatMessage(chatMessage.id)
                },
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                  Icon(
                    imageVector = HedvigIcons.Refresh,
                    contentDescription = null,
                    tint = HedvigTheme.colorScheme.signalRedElement,
                    modifier = Modifier.size(20.dp),
                  )
                  HedvigText(text = chatMessage.text)
                }
              }
            }

            is ChatMessagePhoto -> {
              FailedToBeSentUri(chatMessage.id, chatMessage.uri, onRetrySendChatMessage, imageLoader)
            }

            is ChatMessageMedia -> {
              FailedToBeSentUri(chatMessage.id, chatMessage.uri, onRetrySendChatMessage, imageLoader)
            }
          }
        }
      }
    },
    uiChatMessage = uiChatMessage,
    chatItemIndex = chatItemIndex,
    modifier = modifier,
  )
}

@Composable
private fun FailedToBeSentUri(
  messageId: String,
  messageUri: Uri,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  imageLoader: ImageLoader,
) {
  val image = HedvigIcons.Refresh
  val retryIconPainter = rememberVectorPainter(
    defaultWidth = image.defaultWidth,
    defaultHeight = image.defaultHeight,
    viewportWidth = image.viewportWidth,
    viewportHeight = image.viewportHeight,
    name = image.name,
    tintColor = Color.White,
    tintBlendMode = image.tintBlendMode,
    autoMirror = image.autoMirror,
    content = { _, _ -> RenderVectorGroup(group = image.root) },
  )
  ChatAsyncImage(
    model = messageUri,
    imageLoader = imageLoader,
    isRetryable = true,
    modifier = Modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .clickable { onRetrySendChatMessage(messageId) }
      .drawWithContent {
        drawContent()
        drawRect(color = Color.Black, alpha = 0.38f)
        withTransform(
          transformBlock = {
            translate(
              left = (size.width - retryIconPainter.intrinsicSize.width) / 2,
              top = (size.height - retryIconPainter.intrinsicSize.height) / 2,
            )
          },
        ) {
          with(retryIconPainter) {
            draw(retryIconPainter.intrinsicSize)
          }
        }
      },
  )
}

@Composable
private fun AttachedFileMessage(
  onClick: () -> Unit,
  containerColor: Color = Color.Transparent,
  borderColor: Color = LocalContentColor.current,
) {
  val shape = HedvigTheme.shapes.cornerLarge
  Box(
    Modifier
      .drawWithCache {
        val stroke = Stroke(1.dp.toPx())
        val outline = shape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
        onDrawWithContent {
          drawContent()
          drawOutline(outline, borderColor, style = stroke)
        }
      }
      .clip(HedvigTheme.shapes.cornerLarge)
      .background(containerColor)
      .clickable(onClick = onClick),
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Icon(imageVector = HedvigIcons.MultipleDocuments, contentDescription = null, modifier = Modifier.size(24.dp))
      HedvigText(text = stringResource(R.string.CHAT_FILE_DOWNLOAD))
    }
  }
}

@Composable
private fun VideoMessage(
  state: MediaState,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier = Modifier,
) {
  LocalLifecycleOwner.current.lifecycle.addObserver(
    object : LifecycleEventObserver {
      override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
          Lifecycle.Event.ON_STOP -> {
            state.player?.pause()
          }

          else -> {}
        }
      }
    },
  )
  val height = if (showingFullWidth) 350.dp else 220.dp
  val updatedModifier = if (showingFullWidth) modifier.padding(horizontal = 16.dp) else modifier
  Media(
    state = state,
    modifier = updatedModifier
      .height(height)
      .clip(HedvigTheme.shapes.cornerLarge)
      .background(Color.Black),
    surfaceType = SurfaceType.TextureView,
    resizeMode = ResizeMode.Fit,
    keepContentOnPlayerReset = true,
    showBuffering = ShowBuffering.Always,
    buffering = {
      Box(Modifier.fillMaxSize(), Alignment.Center) {
        HedvigCircularProgressIndicator()
      }
    },
  ) { state ->
    val controllerState = rememberControllerState(state)
    SimpleVideoController(
      mediaState = state,
      controllerState = controllerState,
      onGoFullWidth = onGoFullWidth,
      onGoDefaultWidth = onGoDefaultWidth,
      showingFullWidth = showingFullWidth,
      modifier = Modifier
        .fillMaxSize()
        .clip(HedvigTheme.shapes.cornerLarge),
    )
  }
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
private fun videoPlayerMediaState(cache: Cache, uri: String): MediaState {
  val httpDataSourceFactory = DefaultHttpDataSource.Factory()
  val cacheDataSourceFactory = CacheDataSource.Factory().setCache(cache)
    .setUpstreamDataSourceFactory(httpDataSourceFactory)
    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
  val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
  val context = LocalContext.current
  val exoPlayer = remember {
    ExoPlayer
      .Builder(context)
      .setMediaSourceFactory(
        mediaSourceFactory,
      )
      .build().apply {
        prepare()
        playWhenReady = false
        repeatMode = Player.REPEAT_MODE_OFF
        setMediaItem(MediaItem.fromUri(uri))
      }
  }
  return rememberMediaState(player = exoPlayer)
}

@Composable
private fun ChatAsyncImage(
  model: Any,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  isRetryable: Boolean = false,
  cacheKey: String? = null,
) {
  val loadedImageIntrinsicSize = remember { mutableStateOf<IntSize?>(null) }
  val placeholderPainter: Painter = rememberShapedColorPainter(HedvigTheme.colorScheme.surfacePrimary)
  val errorPainter: Painter = if (isRetryable) {
    val errorImage = HedvigIcons.Refresh
    val vectorSize = 50.dp
    rememberVectorPainter(
      defaultWidth = vectorSize,
      defaultHeight = vectorSize,
      name = errorImage.name,
      tintColor = HedvigTheme.colorScheme.signalRedElement,
      tintBlendMode = errorImage.tintBlendMode,
      autoMirror = errorImage.autoMirror,
      content = { viewportWidth: Float, viewportHeight: Float ->
        RenderVectorGroup(
          group = errorImage.root,
          configs = mapOf(
            // "" is the default name for vectors, so this applies to the entirety of the [errorImage]
            "" to object : VectorConfig {
              override fun <T> getOrDefault(property: VectorProperty<T>, defaultValue: T): T {
                @Suppress("UNCHECKED_CAST") // TranslateX and TranslateY both have `Float` as their `T`
                return when (property) {
                  // todo chat: vector is almost centered, not quite though, the top left is centered only
                  //  -12.5f is an approximation for now
                  VectorProperty.TranslateX -> ((viewportWidth / 2) - 12.5f) as T
                  VectorProperty.TranslateY -> ((viewportHeight / 2) - 12.5f) as T
                  else -> defaultValue
                }
              }
            },
          ),
        )
      },
    )
  } else {
    rememberShapedColorPainter(HedvigTheme.colorScheme.signalRedFill)
  }
  AsyncImage(
    model = ImageRequest
      .Builder(LocalContext.current)
      .data(model)
      .apply {
        if (cacheKey != null) {
          diskCacheKey(cacheKey).memoryCacheKey(cacheKey)
        }
      }.build(),
    contentDescription = null,
    imageLoader = imageLoader,
    contentScale = ContentScale.Fit,
    transform = { state ->
      when (state) {
        is AsyncImagePainter.State.Loading -> {
          state.copy(painter = placeholderPainter)
        }

        is AsyncImagePainter.State.Error -> {
          loadedImageIntrinsicSize.value = IntSize(0, 0)
          if (state.result.throwable is NullRequestDataException) {
            state.copy(painter = errorPainter)
          } else {
            state.copy(painter = errorPainter)
          }
        }

        AsyncImagePainter.State.Empty -> state
        is AsyncImagePainter.State.Success -> {
          loadedImageIntrinsicSize.value = IntSize(
            state.result.drawable.intrinsicWidth,
            state.result.drawable.intrinsicHeight,
          )
          state
        }
      }
    },
    modifier = Modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .then(modifier)
      .adjustSizeToImageRatio(getImageSize = { loadedImageIntrinsicSize.value })
      .then(
        if (loadedImageIntrinsicSize.value == null) {
          Modifier.hedvigPlaceholder(
            visible = true,
            shape = HedvigTheme.shapes.cornerLarge,
            highlight = PlaceholderHighlight.fade(),
          )
        } else {
          Modifier
        },
      ),
  )
}

@Composable
internal fun ChatMessageWithTimeAndDeliveryStatus(
  messageSlot: @Composable () -> Unit,
  uiChatMessage: CbmUiChatMessage?,
  chatItemIndex: Int,
  modifier: Modifier = Modifier,
) {
  val chatMessage = uiChatMessage?.chatMessage
  Column(
    horizontalAlignment = chatMessage.messageHorizontalAlignment(chatItemIndex),
    modifier = modifier,
  ) {
    val failedToBeSent = chatMessage is CbmChatMessage.FailedToBeSent
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      messageSlot()
      if (failedToBeSent) {
        Spacer(Modifier.width(4.dp))
        Icon(
          imageVector = HedvigIcons.InfoFilled,
          contentDescription = null,
          tint = HedvigTheme.colorScheme.signalRedElement,
          modifier = Modifier.size(16.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .align(chatMessage.messageHorizontalAlignment(chatItemIndex))
        .padding(horizontal = 2.dp),
    ) {
      HedvigText(
        text = buildString {
          if (chatMessage == null) {
            append("HHHH.HH.HH")
          } else {
            if (failedToBeSent) {
              append(stringResource(R.string.CHAT_FAILED_TO_SEND))
              append(" • ")
            }
            append(chatMessage.formattedDateTime(getLocale()))
            if (uiChatMessage.isLastDeliveredMessage) {
              append(" • ")
              append(stringResource(R.string.CHAT_DELIVERED_MESSAGE))
            }
          }
        },
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.then(
          if (chatMessage == null) {
            Modifier.hedvigPlaceholder(
              visible = true,
              shape = HedvigTheme.shapes.cornerSmall,
              highlight = PlaceholderHighlight.shimmer(),
            )
          } else {
            Modifier
          },
        ),
      )
      if (uiChatMessage?.isLastDeliveredMessage == true) {
        Spacer(Modifier.width(4.dp))
        Icon(
          HedvigIcons.CheckFilled,
          null,
          Modifier.size(16.dp),
          tint = HedvigTheme.colorScheme.signalBlueElement,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChatLoadedScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val fakeChatMessages: List<CbmUiChatMessage> = listOf(
        ChatMessageFile("1", MEMBER, Instant.parse("2024-05-01T00:00:00Z"), "", IMAGE),
        ChatMessageGif("2", HEDVIG, Instant.parse("2024-05-01T00:00:00Z"), ""),
        ChatMessageFile("3", MEMBER, Instant.parse("2024-05-01T00:00:00Z"), "", IMAGE),
        ChatMessageMedia("4", Instant.parse("2024-05-01T00:00:00Z"), Uri.EMPTY),
        ChatMessagePhoto("5", Instant.parse("2024-05-01T00:01:00Z"), Uri.EMPTY),
        ChatMessageText("6", Instant.parse("2024-05-01T00:02:00Z"), "Failed message"),
        CbmChatMessage.ChatMessageText("7", HEDVIG, Instant.parse("2024-05-01T00:03:00Z"), "Last message"),
      )
        .reversed()
        .mapIndexed { index, item ->
          CbmUiChatMessage(item, index == 0)
        }
      ChatLoadedScreen(
        uiState = Loaded(
          backendConversationInfo = Info(
            "1",
            ClaimInfo("id", "claimType"),
            Instant.parse("2024-05-01T00:00:00Z"),
            false,
          ),
          messages = flowOf(PagingData.from(fakeChatMessages)).collectAsLazyPagingItems(),
          latestMessage = null,
          bannerText = ClosedConversation,
          showUploading = true,
        ),
        lazyListState = rememberLazyListState(),
        imageLoader = rememberPreviewImageLoader(),
        openUrl = {},
        onNavigateToImageViewer = { _, _ -> },
        onRetrySendChatMessage = {},
        chatInput = {},
        simpleVideoCache = rememberPreviewSimpleCache(),
      )
    }
  }
}
