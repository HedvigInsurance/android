package com.hedvig.android.feature.chat.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.NullRequestDataException
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.material3.rememberShapedColorPainter
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.MultipleDocuments
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.model.ChatMessage
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun ChatLoadedScreen(
  uiState: ChatUiState.Loaded,
  imageLoader: ImageLoader,
  appPackageId: String,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  openUrl: (String) -> Unit,
  onSendMessage: (String) -> Unit,
  onSendFile: (Uri) -> Unit,
  onFetchMoreMessages: () -> Unit,
  onDismissError: () -> Unit,
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
        onFetchMoreMessages = onFetchMoreMessages,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clearFocusOnTap()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
      )
      Divider(Modifier.fillMaxWidth())
      Box(
        propagateMinConstraints = true,
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
      ) {
        ChatInput(
          onSendMessage = onSendMessage,
          onSendFile = onSendFile,
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
private fun ScrollToBottomOnOwnMessageSentEffect(lazyListState: LazyListState, messages: ImmutableList<UiChatMessage>) {
  var currentLatestMessage by remember { mutableStateOf(messages.firstOrNull()) }
  LaunchedEffect(messages) {
    currentLatestMessage = messages.firstOrNull()
  }
  LaunchedEffect(lazyListState) {
    snapshotFlow { currentLatestMessage }
      .filterNotNull()
      .distinctUntilChanged { old, new -> old.chatMessage.id == new.chatMessage.id }
      .filter {
        it.chatMessage.sender == ChatMessage.Sender.MEMBER
      }
      .collectLatest {
        lazyListState.scrollToItem(0)
      }
  }
}

@Composable
private fun ScrollToBottomOnNewMessageReceivedWhenAlreadyAtBottomEffect(
  lazyListState: LazyListState,
  messages: ImmutableList<UiChatMessage>,
) {
  var currentLatestMessage by remember { mutableStateOf(messages.firstOrNull()) }
  LaunchedEffect(messages) {
    currentLatestMessage = messages.firstOrNull()
  }
  LaunchedEffect(lazyListState) {
    snapshotFlow { currentLatestMessage }
      .filterNotNull()
      .distinctUntilChanged { old, new -> old.chatMessage.id == new.chatMessage.id }
      .filter {
        it.chatMessage.sender == ChatMessage.Sender.HEDVIG
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
      contentType = {
        when {
          it.sentStatus == UiChatMessage.SentStatus.NotYetSent -> "BeingSent"
          it.sentStatus == UiChatMessage.SentStatus.FailedToBeSent -> "FailedToBeSent"
          it.chatMessage.sender == ChatMessage.Sender.MEMBER -> "FromMember"
          it.chatMessage.sender == ChatMessage.Sender.HEDVIG -> "FromHedvig"
          else -> null
        }
      },
    ) { uiChatMessage: UiChatMessage ->
      val alignment: Alignment.Horizontal = uiChatMessage.chatMessage.getMessageHorizontalAlignment()
      ChatBubble(
        chatMessage = uiChatMessage.chatMessage,
        imageLoader = imageLoader,
        openUrl = openUrl,
        modifier = Modifier
          .fillParentMaxWidth()
          .padding(horizontal = 16.dp)
          .wrapContentWidth(alignment)
          .fillParentMaxWidth(0.8f)
          .wrapContentWidth(alignment)
          .padding(bottom = 8.dp),
      )
    }
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
        when (uiState.fetchMoreMessagesUiState) {
          ChatUiState.Loaded.FetchMoreMessagesUiState.FailedToFetch -> {
            HedvigErrorSection(
              title = "Failed to fetch more messages",
              subTitle = null,
              contentPadding = PaddingValues(0.dp),
              withDefaultVerticalSpacing = false,
              retry = { onFetchMoreMessages() },
            )
          }

          ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore -> {
            ThreeDotsLoading(
              Modifier
                .padding(24.dp)
                .fillParentMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            )
          }

          ChatUiState.Loaded.FetchMoreMessagesUiState.NothingMoreToFetch -> {}
          ChatUiState.Loaded.FetchMoreMessagesUiState.StillInitializing -> {}
        }
      }
    }
  }
}

@Composable
private fun ChatBubble(
  chatMessage: ChatMessage,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  ChatMessageWithTimeSent(
    messageSlot = {
      when (chatMessage) {
        is ChatMessage.ChatMessageFile -> {
          when (chatMessage.mimeType) {
            ChatMessage.ChatMessageFile.MimeType.IMAGE -> {
              ChatAsyncImage(chatMessage.url, imageLoader)
            }

            ChatMessage.ChatMessageFile.MimeType.PDF, // todo chat: consider rendering PDFs inline if needed
            ChatMessage.ChatMessageFile.MimeType.OTHER,
            -> {
              FileMessage(chatMessage, openUrl)
            }
          }
        }

        is ChatMessage.ChatMessageGif -> {
          ChatAsyncImage(chatMessage.gifUrl, imageLoader)
        }

        is ChatMessage.ChatMessageText -> {
          Surface(
            shape = MaterialTheme.shapes.squircleMedium,
            color = chatMessage.backgroundColor(),
          ) {
            TextWithClickableUrls(
              text = chatMessage.text,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
          }
        }
      }
    },
    chatMessage = chatMessage,
    modifier = modifier,
  )
}

@Composable
private fun FileMessage(chatMessage: ChatMessage.ChatMessageFile, openUri: (String) -> Unit) {
  val shape = MaterialTheme.shapes.squircleMedium
  val contentColor = LocalContentColor.current
  Box(
    Modifier
      .drawWithCache {
        val stroke = Stroke(1.dp.toPx())
        val outline = shape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
        val path = (outline as Outline.Generic).path
        onDrawWithContent {
          drawContent()
          drawPath(path, contentColor, style = stroke)
        }
      }
      .clip(MaterialTheme.shapes.squircleMedium)
      .clickable {
        openUri(chatMessage.url)
      },
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
private fun ChatAsyncImage(imageUrl: String, imageLoader: ImageLoader) {
  val loadedImageIntrinsicSize = remember { mutableStateOf<IntSize?>(null) }
  // todo chat: decide what to show when messages fail to load
  val fallbackAndErrorPainter: Painter = rememberShapedColorPainter(MaterialTheme.colorScheme.errorContainer)
  val placeholder: Painter = rememberShapedColorPainter(MaterialTheme.colorScheme.surface)
  AsyncImage(
    model = imageUrl,
    contentDescription = null,
    imageLoader = imageLoader,
    transform = { state ->
      when (state) {
        is AsyncImagePainter.State.Loading -> {
          state.copy(painter = placeholder)
        }

        is AsyncImagePainter.State.Error -> if (state.result.throwable is NullRequestDataException) {
          // todo chat: fallback for image which is unable to load here
          state.copy(painter = fallbackAndErrorPainter)
        } else {
          // todo chat: painter to show when the network request of the image failed here
          state.copy(painter = fallbackAndErrorPainter)
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
      .adjustSizeToImageRatioOrShowPlaceholder(getImageSize = { loadedImageIntrinsicSize.value })
      .clip(MaterialTheme.shapes.squircleMedium),
  )
}

@Composable
internal fun ChatMessageWithTimeSent(
  messageSlot: @Composable () -> Unit,
  chatMessage: ChatMessage,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    messageSlot()
    Spacer(modifier = Modifier.height(4.dp))
    val locale = getLocale()
    Text(
      text = chatMessage.formattedDateTime(locale),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(chatMessage.getMessageHorizontalAlignment())
        .padding(horizontal = 2.dp),
    )
  }
}
