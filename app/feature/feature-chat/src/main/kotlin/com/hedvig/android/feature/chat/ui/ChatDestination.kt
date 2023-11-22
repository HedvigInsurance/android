package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.debugBorder
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.chat.ChatEventNew
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.data.ChatMessage
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import java.io.File
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import org.koin.compose.koinInject

@Composable
internal fun ChatDestination(viewModel: ChatViewModel, imageLoader: ImageLoader, onNavigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChatScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    onNavigateUp = onNavigateUp,
    onSendMessage = { message: String ->
      viewModel.emit(ChatEventNew.SendTextMessage(message))
    },
    onSendFile = { file: File ->
      viewModel.emit(ChatEventNew.SendFileMessage(file))
    },
    onFetchMoreMessages = {
      viewModel.emit(ChatEventNew.FetchMoreMessages)
    },
    onDismissError = {
      viewModel.emit(ChatEventNew.DismissError)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatScreen(
  uiState: ChatUiState,
  imageLoader: ImageLoader,
  onNavigateUp: () -> Unit,
  onSendMessage: (String) -> Unit,
  onSendFile: (File) -> Unit,
  onFetchMoreMessages: () -> Unit,
  onDismissError: () -> Unit,
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
        onHeightChanged = { height -> with(density) { topAppBarHeight = height.toDp() } },
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .consumeWindowInsets(PaddingValues(top = topAppBarHeight)),
        propagateMinConstraints = true,
      ) {
        when (uiState) {
          ChatUiState.DemoMode -> {
            Box(
              Modifier
                .fillMaxSize()
                .debugBorder(),
              contentAlignment = Alignment.Center,
            ) {
              Text("Demo mode")
            }
          }

          ChatUiState.DisabledByFeatureFlag -> {
            Box(
              Modifier
                .fillMaxSize()
                .debugBorder(),
              contentAlignment = Alignment.Center,
            ) {
              Text("Chat disabled by feature flag")
            }
          }

          ChatUiState.Initializing -> {
            HedvigFullScreenCenterAlignedProgress()
          }

          is ChatUiState.Loaded -> {
            ChatLoadedScreen(
              uiState,
              imageLoader,
              topAppBarScrollBehavior,
              onSendMessage,
              onSendFile,
              onFetchMoreMessages,
              onDismissError,
            )
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
  onHeightChanged: (Int) -> Unit,
) {
  TopAppBarWithBack(
    title = stringResource(R.string.CHAT_TITLE),
    onClick = onNavigateUp,
    scrollBehavior = topAppBarScrollBehavior,
    windowInsets = chatTopAppBarWindowInsets(TopAppBarDefaults.windowInsets, topAppBarScrollBehavior),
    modifier = Modifier
      .fillMaxWidth()
      .onSizeChanged {
        onHeightChanged(it.height)
      },
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
        onNavigateUp = {},
        onSendMessage = {},
        onSendFile = {},
        onFetchMoreMessages = {},
        onDismissError = {},
      )
    }
  }
}

private class ChatUiStateProvider : CollectionPreviewParameterProvider<ChatUiState>(
  listOf(
    ChatUiState.Initializing,
    ChatUiState.DisabledByFeatureFlag,
    ChatUiState.Loaded(
      List(10) { index ->
        UiChatMessage(
          ChatMessage.ChatMessageText(
            id = index.toString(),
            sender = if (index % 2 == 0 || index > 7) ChatMessage.Sender.MEMBER else ChatMessage.Sender.HEDVIG,
            sentAt = Clock.System.now().plus(index.seconds),
            text = "Hello #$index" + if (index == 0) {
              "long".repeat(15)
            } else {
              ""
            },
          ),
          sentStatus = when (index) {
            0 -> UiChatMessage.SentStatus.NotYetSent
            1 -> UiChatMessage.SentStatus.FailedToBeSent
            else -> UiChatMessage.SentStatus.Sent
          },
        )
      }.toImmutableList(),
      errorMessage = null,
      fetchMoreMessagesUiState = ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore,
    ),
  ),
)
