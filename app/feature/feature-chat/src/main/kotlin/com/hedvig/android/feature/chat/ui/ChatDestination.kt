package com.hedvig.android.feature.chat.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.chat.ChatEvent
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.model.ChatMessage
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock

@Composable
internal fun ChatDestination(
  viewModel: ChatViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChatScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    openUrl = openUrl,
    onNavigateUp = onNavigateUp,
    onSendMessage = { message: String ->
      viewModel.emit(ChatEvent.SendTextMessage(message))
    },
    onSendPhoto = { uri: Uri ->
      logcat { "viewModel.emit(ChatEvent.SendPhotoMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      viewModel.emit(ChatEvent.SendPhotoMessage(uri))
    },
    onSendMedia = { uri: Uri ->
      logcat { "viewModel.emit(ChatEvent.SendMediaMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      viewModel.emit(ChatEvent.SendMediaMessage(uri))
    },
    onRetrySendChatMessage = {
      viewModel.emit(ChatEvent.RetrySend(it))
    },
    onFetchMoreMessages = {
      viewModel.emit(ChatEvent.FetchMoreMessages)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatScreen(
  uiState: ChatUiState,
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateUp: () -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (Uri) -> Unit,
  onSendMedia: (Uri) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onFetchMoreMessages: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    val topAppBarScrollBehavior = TopAppBarDefaults.chatScrollBehavior()
    Column {
      val density = LocalDensity.current
      var topAppBarHeight by remember { mutableStateOf(0.dp) }
      ChatTopAppBar(
        onNavigateUp = onNavigateUp,
        topAppBarScrollBehavior = topAppBarScrollBehavior,
        modifier = Modifier.onSizeChanged {
          with(density) { topAppBarHeight = it.height.toDp() }
        },
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .consumeWindowInsets(PaddingValues(top = topAppBarHeight)),
        propagateMinConstraints = true,
      ) {
        val loadingIndicator = remember { movableContentOf { HedvigFullScreenCenterAlignedProgress() } }
        when (uiState) {
          ChatUiState.Initializing -> {
            loadingIndicator()
          }

          is ChatUiState.Loaded -> {
            ChatLoadedScreen(
              uiState = uiState,
              imageLoader = imageLoader,
              appPackageId = appPackageId,
              topAppBarScrollBehavior = topAppBarScrollBehavior,
              openUrl = openUrl,
              onRetrySendChatMessage = onRetrySendChatMessage,
              onSendMessage = onSendMessage,
              onSendPhoto = onSendPhoto,
              onSendMedia = onSendMedia,
              onFetchMoreMessages = onFetchMoreMessages,
            )
            val stillLoadingInitialMessages = uiState.messages.isEmpty() &&
              (uiState.fetchMoreMessagesUiState is ChatUiState.Loaded.FetchMoreMessagesUiState.StillInitializing ||
                uiState.fetchMoreMessagesUiState is ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore)
            if (stillLoadingInitialMessages) {
              loadingIndicator()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChatTopAppBar(
  onNavigateUp: () -> Unit,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  TopAppBarWithBack(
    title = stringResource(R.string.CHAT_TITLE),
    onClick = onNavigateUp,
    scrollBehavior = topAppBarScrollBehavior,
    windowInsets = chatTopAppBarWindowInsets(TopAppBarDefaults.windowInsets, topAppBarScrollBehavior),
    modifier = modifier.fillMaxWidth(),
  )
}

@HedvigPreview
@Composable
private fun ChatScreenPreview(
  @PreviewParameter(ChatUiStateProvider::class) chatUiState: ChatUiState,
) {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.fillMaxSize(),
    ) {
      ChatScreen(
        uiState = chatUiState,
        imageLoader = rememberPreviewImageLoader(),
        appPackageId = "com.hedvig",
        openUrl = {},
        onNavigateUp = {},
        onSendMessage = {},
        onSendPhoto = {},
        onSendMedia = {},
        onRetrySendChatMessage = {},
        onFetchMoreMessages = {},
      )
    }
  }
}

private class ChatUiStateProvider : CollectionPreviewParameterProvider<ChatUiState>(
  listOf(
    ChatUiState.Initializing,
    ChatUiState.Loaded(
      List(10) { index ->
        ChatMessage.ChatMessageText(
          id = index.toString(),
          sender = if (index % 2 == 0 || index > 7) ChatMessage.Sender.MEMBER else ChatMessage.Sender.HEDVIG,
          sentAt = Clock.System.now().plus(index.seconds),
          text = "Hello #$index" + if (index == 0) {
            "long".repeat(15)
          } else {
            ""
          },
        )
      }
        .plus(
          ChatMessage.FailedToBeSent.ChatMessageText(
            id = "failed",
            sentAt = Clock.System.now(),
            text = "Failed to be sent",
          ),
        )
        .map {
          ChatUiState.Loaded.UiChatMessage(it, false)
        }
        .toImmutableList(),
      bannerText = "Test",
      fetchMoreMessagesUiState = ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore,
    ),
  ),
)
