package com.hedvig.android.feature.chat.cbm.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorConfig
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.NullRequestDataException
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.rememberShapedColorPainter
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.CircleWithCheckmarkFilled
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.normal.MultipleDocuments
import com.hedvig.android.core.icons.hedvig.normal.RestartOneArrow
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.layout.adjustSizeToImageRatio
import com.hedvig.android.feature.chat.cbm.CbmChatUiState
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded.LatestChatMessage
import com.hedvig.android.feature.chat.cbm.CbmUiChatMessage
import com.hedvig.android.feature.chat.cbm.model.CbmChatMessage
import com.hedvig.android.feature.chat.cbm.model.Sender
import com.hedvig.android.feature.chat.ui.ChatBanner
import com.hedvig.android.feature.chat.ui.ChatInput
import com.hedvig.android.feature.chat.ui.TextWithClickableUrls
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder
import com.hedvig.android.placeholder.shimmer
import hedvig.resources.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Composable
internal fun CbmChatLoadedScreen(
  uiState: CbmChatUiState.Loaded,
  imageLoader: ImageLoader,
  appPackageId: String,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  openUrl: (String) -> Unit,
  onBannerLinkClicked: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (Uri) -> Unit,
  onSendMedia: (Uri) -> Unit,
) {
  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val scrollToBottom = {
    coroutineScope.launch { lazyListState.scrollToItem(0) }
  }
  ChatLoadedScreen(
    uiState = uiState,
    lazyListState = lazyListState,
    imageLoader = imageLoader,
    topAppBarScrollBehavior = topAppBarScrollBehavior,
    openUrl = openUrl,
    onBannerLinkClicked = onBannerLinkClicked,
    onRetrySendChatMessage = onRetrySendChatMessage,
    chatInput = {
      ChatInput(
        onSendMessage = {
          onSendMessage(it)
          scrollToBottom()
        },
        onSendPhoto = {
          onSendPhoto(it)
          scrollToBottom()
        },
        onSendMedia = {
          onSendMedia(it)
          scrollToBottom()
        },
        appPackageId = appPackageId,
        modifier = Modifier.padding(16.dp),
      )
    },
  )
}

@Composable
private fun ChatLoadedScreen(
  uiState: CbmChatUiState.Loaded,
  lazyListState: LazyListState,
  imageLoader: ImageLoader,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  openUrl: (String) -> Unit,
  onBannerLinkClicked: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  chatInput: @Composable () -> Unit,
) {
  SelectionContainer {
    Column {
      ChatLazyColumn(
        lazyListState = lazyListState,
        messages = uiState.messages,
        latestChatMessage = uiState.latestMessage,
        imageLoader = imageLoader,
        openUrl = openUrl,
        onRetrySendChatMessage = onRetrySendChatMessage,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clearFocusOnTap()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
      )
      if (uiState.bannerText != null) {
        HorizontalDivider(Modifier.fillMaxWidth())
        ChatBanner(
          text = uiState.bannerText,
          onBannerLinkClicked = onBannerLinkClicked,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      HorizontalDivider(Modifier.fillMaxWidth())
      Box(
        propagateMinConstraints = true,
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
      ) {
        chatInput()
      }
    }
  }
}

@Composable
private fun ScrollToBottomEffect(lazyListState: LazyListState, latestChatMessage: LatestChatMessage?) {
  val updatedLatestChatMessage by rememberUpdatedState(latestChatMessage)
  LaunchedEffect(lazyListState) {
    snapshotFlow { updatedLatestChatMessage }
      .filterNotNull()
      .distinctUntilChangedBy(LatestChatMessage::id)
      .collectLatest { chatMessage ->
        val senderIsMember = chatMessage.sender == Sender.MEMBER
        val isAlreadyCloseToTheBottom = lazyListState.firstVisibleItemIndex <= 2
        if (senderIsMember || isAlreadyCloseToTheBottom) {
          lazyListState.scrollToItem(0)
        }
      }
  }
}

@Composable
private fun ChatLazyColumn(
  lazyListState: LazyListState,
  messages: LazyPagingItems<CbmUiChatMessage>,
  latestChatMessage: LatestChatMessage?,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  ScrollToBottomEffect(
    lazyListState = lazyListState,
    latestChatMessage = latestChatMessage,
  )
  val loadingMore by remember(messages) {
    derivedStateOf { messages.loadState.append == LoadState.Loading }
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
          is CbmChatMessage.ChatMessageFile -> "ChatMessage.ChatMessageFile"
          is CbmChatMessage.ChatMessageGif -> "ChatMessage.ChatMessageGif"
          is CbmChatMessage.ChatMessageText -> "ChatMessage.ChatMessageText"
          is CbmChatMessage.FailedToBeSent.ChatMessageText -> "ChatMessage.FailedToBeSent.ChatMessageText"
          is CbmChatMessage.FailedToBeSent.ChatMessagePhoto -> "ChatMessage.FailedToBeSent.ChatMessagePhoto"
          is CbmChatMessage.FailedToBeSent.ChatMessageMedia -> "ChatMessage.FailedToBeSent.ChatMessageMedia"
        }
      },
    ) { index: Int ->
      val uiChatMessage = messages[index]
      val alignment: Alignment.Horizontal = uiChatMessage?.chatMessage.messageHorizontalAlignment(index)
      ChatBubble(
        uiChatMessage = uiChatMessage,
        chatItemIndex = index,
        imageLoader = imageLoader,
        openUrl = openUrl,
        onRetrySendChatMessage = onRetrySendChatMessage,
        modifier = Modifier
          .fillParentMaxWidth()
          .padding(horizontal = 16.dp)
          .wrapContentWidth(alignment)
          .fillParentMaxWidth(0.8f)
          .wrapContentWidth(alignment)
          .padding(bottom = 8.dp),
      )
    }
    if (loadingMore) {
      item {
        Box(
          Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .padding(24.dp),
        ) {
          ThreeDotsLoading(
            Modifier
              .padding(24.dp)
              .fillParentMaxWidth()
              .wrapContentWidth(Alignment.CenterHorizontally),
          )
        }
      }
    }
  }
}

@Composable
private fun ChatBubble(
  uiChatMessage: CbmUiChatMessage?,
  chatItemIndex: Int,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val chatMessage = uiChatMessage?.chatMessage
  ChatMessageWithTimeAndDeliveryStatus(
    messageSlot = {
      when (chatMessage) {
        null -> {
          Box(
            Modifier
              .clip(shape = MaterialTheme.shapes.squircleMedium)
              .placeholder(true, highlight = PlaceholderHighlight.shimmer()),
          ) {
            Text(
              text = "HHHHHHHHHH",
              style = LocalTextStyle.current,
              modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .withoutPlacement(),
            )
          }
        }

        is CbmChatMessage.ChatMessageText -> {
          Surface(
            shape = MaterialTheme.shapes.squircleMedium,
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

        is CbmChatMessage.ChatMessageFile -> {
          when (chatMessage.mimeType) {
            CbmChatMessage.ChatMessageFile.MimeType.IMAGE -> {
              ChatAsyncImage(model = chatMessage.url, imageLoader = imageLoader, cacheKey = chatMessage.id)
            }

            CbmChatMessage.ChatMessageFile.MimeType.PDF, // todo chat: consider rendering PDFs inline in the chat
            CbmChatMessage.ChatMessageFile.MimeType.MP4, // todo chat: consider rendering videos inline in the chat
            CbmChatMessage.ChatMessageFile.MimeType.OTHER,
            -> {
              AttachedFileMessage(onClick = { openUrl(chatMessage.url) })
            }
          }
        }

        is CbmChatMessage.ChatMessageGif -> {
          ChatAsyncImage(model = chatMessage.gifUrl, imageLoader = imageLoader, cacheKey = chatMessage.id)
        }

        is CbmChatMessage.FailedToBeSent -> {
          when (chatMessage) {
            is CbmChatMessage.FailedToBeSent.ChatMessageText -> {
              Surface(
                shape = MaterialTheme.shapes.squircleMedium,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
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
                    imageVector = Icons.Hedvig.RestartOneArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                  )
                  Text(text = chatMessage.text)
                }
              }
            }

            is CbmChatMessage.FailedToBeSent.ChatMessagePhoto -> {
              FailedToBeSentUri(chatMessage.id, chatMessage.uri, onRetrySendChatMessage, imageLoader)
            }

            is CbmChatMessage.FailedToBeSent.ChatMessageMedia -> {
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
  val image = Icons.Hedvig.RestartOneArrow
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
      .clip(MaterialTheme.shapes.squircleMedium)
      .clickable { onRetrySendChatMessage(messageId) }
      .drawWithContent {
        drawContent()
        drawRect(color = Color.Black, alpha = DisabledAlpha)
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
  val shape = MaterialTheme.shapes.squircleMedium
  Box(
    Modifier
      .drawWithCache {
        val stroke = Stroke(1.dp.toPx())
        val outline = shape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
        val path = (outline as Outline.Generic).path
        onDrawWithContent {
          drawContent()
          drawPath(path, borderColor, style = stroke)
        }
      }.clip(MaterialTheme.shapes.squircleMedium)
      .background(containerColor)
      .clickable(onClick = onClick),
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Icon(imageVector = Icons.Hedvig.MultipleDocuments, contentDescription = null, modifier = Modifier.size(24.dp))
      Text(text = stringResource(R.string.CHAT_FILE_DOWNLOAD))
    }
  }
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
  val placeholderPainter: Painter = rememberShapedColorPainter(MaterialTheme.colorScheme.surface)
  val errorPainter: Painter = if (isRetryable) {
    val errorImage = Icons.Hedvig.RestartOneArrow
    val vectorSize = 50.dp
    rememberVectorPainter(
      defaultWidth = vectorSize,
      defaultHeight = vectorSize,
      name = errorImage.name,
      tintColor = MaterialTheme.colorScheme.error,
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
    rememberShapedColorPainter(MaterialTheme.colorScheme.errorContainer)
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
    modifier = modifier
      .adjustSizeToImageRatio(getImageSize = { loadedImageIntrinsicSize.value })
      .then(
        if (loadedImageIntrinsicSize.value == null) {
          Modifier.placeholder(visible = true, highlight = PlaceholderHighlight.fade())
        } else {
          Modifier
        },
      ).clip(MaterialTheme.shapes.squircleMedium),
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
          imageVector = Icons.Hedvig.InfoFilled,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
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
      Text(
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
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.then(
          if (chatMessage == null) {
            Modifier.placeholder(visible = true, highlight = PlaceholderHighlight.shimmer())
          } else {
            Modifier
          },
        ),
      )
      if (uiChatMessage?.isLastDeliveredMessage == true) {
        Spacer(Modifier.width(4.dp))
        Icon(
          Icons.Hedvig.CircleWithCheckmarkFilled,
          null,
          Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.infoElement,
        )
      }
    }
  }
}

// todo cbm preview
