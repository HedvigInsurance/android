package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Divider
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.debugBorder
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.data.ChatMessage
import java.io.File
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun ChatLoadedScreen(
  uiState: ChatUiState.Loaded,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  onSendMessage: (String) -> Unit,
  onSendFile: (File) -> Unit,
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
        onFetchMoreMessages = onFetchMoreMessages,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clearFocusOnTap()
          .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
          .debugBorder(dp = 3.dp),
      )
      Divider(Modifier.fillMaxWidth())
      Box(
        propagateMinConstraints = true,
        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
      ) {
        ChatTextInput(
          onSendMessage = { onSendMessage(it) },
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
      val alignment: Alignment.Horizontal = uiChatMessage.chatMessage.getMessageAlignment()
      ChatBubble(
        chatMessage = uiChatMessage.chatMessage,
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
private fun ChatBubble(chatMessage: ChatMessage, modifier: Modifier = Modifier) {
  when (chatMessage) {
    is ChatMessage.ChatMessageFile -> Text("file todo", modifier)
    is ChatMessage.ChatMessageText -> TextMessage(chatMessage, modifier)
  }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TextMessage(chatMessage: ChatMessage.ChatMessageText, modifier: Modifier = Modifier) {
  val locale = getLocale()
  Column(modifier) {
    Surface(
      shape = MaterialTheme.shapes.squircleMedium,
      color = chatMessage.backgroundColor(),
    ) {
      TextWithClickableUrls(
        text = chatMessage.text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = chatMessage.formattedDateTime(locale),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(chatMessage.getMessageAlignment())
        .padding(horizontal = 2.dp),
    )
  }
}
