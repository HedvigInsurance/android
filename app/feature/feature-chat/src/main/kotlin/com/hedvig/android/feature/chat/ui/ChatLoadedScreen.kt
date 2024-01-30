package com.hedvig.android.feature.chat.ui

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.NullRequestDataException
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.rememberShapedColorPainter
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.CircleWithCheckmarkFilled
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.normal.MultipleDocuments
import com.hedvig.android.core.icons.hedvig.normal.RestartOneArrow
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.layout.adjustSizeToImageRatio
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock

@Composable
internal fun ChatLoadedScreen(
  uiState: ChatUiState.Loaded,
  imageLoader: ImageLoader,
  appPackageId: String,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  openUrl: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (Uri) -> Unit,
  onSendMedia: (Uri) -> Unit,
  onFetchMoreMessages: () -> Unit,
) {
  val lazyListState = rememberLazyListState()

  ScrollToBottomOnKeyboardShownEffect(lazyListState = lazyListState)
  ScrollToBottomOnOwnMessageSentEffect(
    lazyListState = lazyListState,
    messages = uiState.messages,
  )
  ScrollToBottomOnNewMessageReceivedWhenAlreadyAtBottomEffect(
    lazyListState = lazyListState,
    messages = uiState.messages,
  )

  SelectionContainer {
    Column {
      ChatLazyColumn(
        lazyListState = lazyListState,
        uiState = uiState,
        imageLoader = imageLoader,
        openUrl = openUrl,
        onRetrySendChatMessage = onRetrySendChatMessage,
        onFetchMoreMessages = onFetchMoreMessages,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clearFocusOnTap()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
      )
      uiState.bannerText?.let {
        Divider(Modifier.fillMaxWidth())
        ChatBanner(
          text = it,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Divider(Modifier.fillMaxWidth())
      Box(
        propagateMinConstraints = true,
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
      ) {
        ChatInput(
          onSendMessage = onSendMessage,
          onSendPhoto = onSendPhoto,
          onSendMedia = onSendMedia,
          appPackageId = appPackageId,
          modifier = Modifier.padding(16.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScrollToBottomOnKeyboardShownEffect(lazyListState: LazyListState) {
  val imeVisibleState = rememberUpdatedState(WindowInsets.isImeVisible)
  LaunchedEffect(Unit) {
    snapshotFlow { imeVisibleState.value }.collectLatest { isImeVisible ->
      if (isImeVisible) {
        lazyListState.scrollToItem(0)
      }
    }
  }
}

@Composable
private fun ScrollToBottomOnOwnMessageSentEffect(
  lazyListState: LazyListState,
  messages: ImmutableList<ChatUiState.Loaded.UiChatMessage>,
) {
  var currentLatestMessage by remember { mutableStateOf(messages.firstOrNull()) }
  LaunchedEffect(messages) {
    currentLatestMessage = messages.firstOrNull()
  }
  LaunchedEffect(lazyListState) {
    snapshotFlow { currentLatestMessage }
      .filterNotNull()
      .map { it.chatMessage }
      .distinctUntilChanged { old, new -> old.id == new.id }
      .filter {
        it.sender == ChatMessage.Sender.MEMBER
      }
      .collectLatest {
        lazyListState.scrollToItem(0)
      }
  }
}

@Composable
private fun ScrollToBottomOnNewMessageReceivedWhenAlreadyAtBottomEffect(
  lazyListState: LazyListState,
  messages: ImmutableList<ChatUiState.Loaded.UiChatMessage>,
) {
  var currentLatestMessage by remember { mutableStateOf(messages.firstOrNull()) }
  LaunchedEffect(messages) {
    currentLatestMessage = messages.firstOrNull()
  }
  LaunchedEffect(lazyListState) {
    snapshotFlow { currentLatestMessage }
      .filterNotNull()
      .map { it.chatMessage }
      .distinctUntilChanged { old, new -> old.id == new.id }
      .filter {
        it.sender == ChatMessage.Sender.HEDVIG
      }
      .collectLatest {
        val isAlreadyCloseToTheBottom = lazyListState.firstVisibleItemIndex <= 2
        if (isAlreadyCloseToTheBottom) {
          lazyListState.scrollToItem(0)
        }
      }
  }
}

@Composable
private fun ChatLazyColumn(
  lazyListState: LazyListState,
  uiState: ChatUiState.Loaded,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onFetchMoreMessages: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    state = lazyListState,
    reverseLayout = true,
    contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues(),
    modifier = modifier,
  ) {
    items(
      items = uiState.messages,
      key = { it.chatMessage.id },
      contentType = { uiChatMessage ->
        when (uiChatMessage.chatMessage) {
          is ChatMessage.ChatMessageFile -> "ChatMessage.ChatMessageFile"
          is ChatMessage.ChatMessageGif -> "ChatMessage.ChatMessageGif"
          is ChatMessage.ChatMessageText -> "ChatMessage.ChatMessageText"
          is ChatMessage.FailedToBeSent.ChatMessageText -> "ChatMessage.FailedToBeSent.ChatMessageText"
          is ChatMessage.FailedToBeSent.ChatMessageUri -> "ChatMessage.FailedToBeSent.ChatMessageUri"
        }
      },
    ) { uiChatMessage: ChatUiState.Loaded.UiChatMessage ->
      val alignment: Alignment.Horizontal = uiChatMessage.chatMessage.messageHorizontalAlignment()
      ChatBubble(
        uiChatMessage = uiChatMessage,
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
    // We want to show other items only when there are already some chat messages, to have first messages appear at the
    // bottom of the UI when the screen is first opened
    if (uiState.messages.isNotEmpty()) {
      item(
        key = "Space",
        contentType = "Space",
      ) {
        Spacer(modifier = Modifier.height(8.dp))
      }
      if (
        uiState.fetchMoreMessagesUiState is ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch ||
        uiState.fetchMoreMessagesUiState is ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore
      ) {
        item(
          key = "FetchingState",
          contentType = "FetchingState",
        ) {
          LaunchedEffect(Unit) {
            onFetchMoreMessages()
          }
          if (uiState.fetchMoreMessagesUiState is ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch) {
            LaunchedEffect(Unit) {
              while (isActive) {
                delay(5.seconds)
                onFetchMoreMessages()
              }
            }
          }
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
  uiChatMessage: ChatUiState.Loaded.UiChatMessage,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  ChatMessageWithTimeAndDeliveryStatus(
    messageSlot = {
      val chatMessage = uiChatMessage.chatMessage
      when (chatMessage) {
        is ChatMessage.ChatMessageFile -> {
          when (chatMessage.mimeType) {
            ChatMessage.ChatMessageFile.MimeType.IMAGE -> {
              ChatAsyncImage(model = chatMessage.url, imageLoader = imageLoader, cacheKey = chatMessage.id)
            }

            ChatMessage.ChatMessageFile.MimeType.PDF, // todo chat: consider rendering PDFs inline in the chat
            ChatMessage.ChatMessageFile.MimeType.MP4, // todo chat: consider rendering videos inline in the chat
            ChatMessage.ChatMessageFile.MimeType.OTHER,
            -> {
              AttachedFileMessage(onClick = { openUrl(chatMessage.url) })
            }
          }
        }

        is ChatMessage.ChatMessageGif -> {
          ChatAsyncImage(model = chatMessage.gifUrl, imageLoader = imageLoader, cacheKey = chatMessage.id)
        }

        is ChatMessage.ChatMessageText -> {
          Surface(
            shape = MaterialTheme.shapes.squircleMedium,
            color = chatMessage.backgroundColor(),
            contentColor = chatMessage.onBackgroundColor(),
          ) {
            TextWithClickableUrls(
              text = chatMessage.text,
              style = LocalTextStyle.current.copy(color = LocalContentColor.current),
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
          }
        }

        is ChatMessage.FailedToBeSent -> {
          when (chatMessage) {
            is ChatMessage.FailedToBeSent.ChatMessageText -> {
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

            is ChatMessage.FailedToBeSent.ChatMessageUri -> {
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
                model = chatMessage.uri,
                imageLoader = imageLoader,
                isRetryable = true,
                modifier = Modifier
                  .clip(MaterialTheme.shapes.squircleMedium)
                  .clickable { onRetrySendChatMessage(chatMessage.id) }
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
          }
        }
      }
    },
    uiChatMessage = uiChatMessage,
    modifier = modifier,
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
      }
      .clip(MaterialTheme.shapes.squircleMedium)
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
    model = ImageRequest.Builder(LocalContext.current)
      .data(model)
      .apply {
        if (cacheKey != null) {
          diskCacheKey(cacheKey).memoryCacheKey(cacheKey)
        }
      }
      .build(),
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
      )
      .clip(MaterialTheme.shapes.squircleMedium),
  )
}

@Composable
internal fun ChatMessageWithTimeAndDeliveryStatus(
  messageSlot: @Composable () -> Unit,
  uiChatMessage: ChatUiState.Loaded.UiChatMessage,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = uiChatMessage.chatMessage.messageHorizontalAlignment(),
    modifier = modifier,
  ) {
    val failedToBeSent = uiChatMessage.chatMessage is ChatMessage.FailedToBeSent
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
        .align(uiChatMessage.chatMessage.messageHorizontalAlignment())
        .padding(horizontal = 2.dp),
    ) {
      Text(
        text = buildString {
          if (failedToBeSent) {
            append(stringResource(R.string.CHAT_FAILED_TO_SEND))
            append(" • ")
          }
          append(uiChatMessage.chatMessage.formattedDateTime(getLocale()))
          if (uiChatMessage.isLastDeliveredMessage) {
            append(" • ")
            append(stringResource(R.string.CHAT_DELIVERED_MESSAGE))
          }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (uiChatMessage.isLastDeliveredMessage) {
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

@HedvigPreview
@Composable
private fun PreviewChatLazyColumn() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      val listSize = 10
      ChatLazyColumn(
        lazyListState = rememberLazyListState(),
        uiState = ChatUiState.Loaded(
          messages = List(listSize) { index ->
            ChatUiState.Loaded.UiChatMessage(
              chatMessage = ChatMessage.ChatMessageText(
                index.toString(),
                when (index % 2 == 0) {
                  true -> ChatMessage.Sender.MEMBER
                  false -> ChatMessage.Sender.HEDVIG
                },
                Clock.System.now(),
                "Hello #$index",
              ),
              isLastDeliveredMessage = index == 0,
            )
          }.toPersistentList(),
          bannerText = null,
          fetchMoreMessagesUiState = ChatUiState.Loaded.FetchMoreMessagesUiState.NothingMoreToFetch,
        ),
        imageLoader = rememberPreviewImageLoader(),
        openUrl = {},
        onRetrySendChatMessage = {},
        onFetchMoreMessages = {},
      )
    }
  }
}
