package com.hedvig.android.feature.chat.cbm.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.feature.chat.cbm.BannerText.Text
import com.hedvig.android.feature.chat.cbm.CbmChatEvent
import com.hedvig.android.feature.chat.cbm.CbmChatUiState
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Error
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Initializing
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded.TopAppBarText
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded.TopAppBarText.Legacy
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded.TopAppBarText.Unknown
import com.hedvig.android.feature.chat.cbm.CbmChatViewModel
import com.hedvig.android.feature.chat.ui.chatScrollBehavior
import com.hedvig.android.feature.chat.ui.chatTopAppBarWindowInsets
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
internal fun CbmChatDestination(
  viewModel: CbmChatViewModel,
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
    onBannerLinkClicked = openUrl,
    onNavigateUp = onNavigateUp,
    onSendMessage = { message: String ->
      viewModel.emit(CbmChatEvent.SendTextMessage(message))
    },
    onSendPhoto = { uri: Uri ->
      logcat { "viewModel.emit(ChatEvent.SendPhotoMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      viewModel.emit(CbmChatEvent.SendPhotoMessage(uri))
    },
    onSendMedia = { uri: Uri ->
      logcat { "viewModel.emit(CbmChatEvent.SendMediaMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      viewModel.emit(CbmChatEvent.SendMediaMessage(uri))
    },
    onRetrySendChatMessage = { messageId ->
      viewModel.emit(CbmChatEvent.RetrySendChatMessage(messageId))
    },
    onRetryLoadingChat = {
      viewModel.emit(CbmChatEvent.RetryLoadingChat)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatScreen(
  uiState: CbmChatUiState,
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onBannerLinkClicked: (String) -> Unit,
  onNavigateUp: () -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (Uri) -> Unit,
  onSendMedia: (Uri) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onRetryLoadingChat: () -> Unit,
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
        uiState = uiState,
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
        when (uiState) {
          Initializing -> {
            HedvigFullScreenCenterAlignedProgress()
          }
          Error -> {
            HedvigErrorSection(onRetryLoadingChat)
          }
          is Loaded -> {
            CbmChatLoadedScreen(
              uiState = uiState,
              imageLoader = imageLoader,
              appPackageId = appPackageId,
              topAppBarScrollBehavior = topAppBarScrollBehavior,
              openUrl = openUrl,
              onBannerLinkClicked = onBannerLinkClicked,
              onRetrySendChatMessage = onRetrySendChatMessage,
              onSendMessage = onSendMessage,
              onSendPhoto = onSendPhoto,
              onSendMedia = onSendMedia,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatTopAppBar(
  uiState: CbmChatUiState,
  onNavigateUp: () -> Unit,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  TopAppBarWithBack(
    title = when (uiState) {
      is Loaded -> when (val topAppBarText = uiState.topAppBarText) {
        Legacy -> stringResource(R.string.CHAT_CONVERSATION_HISTORY_TITLE)
        is TopAppBarText.Text -> topAppBarText.title
        Unknown -> stringResource(R.string.CHAT_TITLE)
      }
      else -> stringResource(R.string.CHAT_TITLE)
    },
    onClick = onNavigateUp,
    scrollBehavior = topAppBarScrollBehavior,
    windowInsets = chatTopAppBarWindowInsets(TopAppBarDefaults.windowInsets, topAppBarScrollBehavior),
    modifier = modifier.fillMaxWidth(),
  )
}

// todo cbm preview
// @HedvigPreview
// @Composable
// private fun ChatScreenPreview(
//  @PreviewParameter(ChatUiStateProvider::class) chatUiState: ChatUiState,
// ) {
//  HedvigTheme {
//    Surface(
//      color = MaterialTheme.colorScheme.background,
//      modifier = Modifier.fillMaxSize(),
//    ) {
//      ChatScreen(
//        uiState = chatUiState,
//        imageLoader = rememberPreviewImageLoader(),
//        appPackageId = "com.hedvig",
//        openUrl = {},
//        onBannerLinkClicked = {},
//        onNavigateUp = {},
//        onSendMessage = {},
//        onSendPhoto = {},
//        onSendMedia = {},
//        onRetrySendChatMessage = {},
//        onFetchMoreMessages = {},
//      )
//    }
//  }
// }
//
// private class ChatUiStateProvider :
//  CollectionPreviewParameterProvider<ChatUiState>(
//    listOf(
//      ChatUiState.Initializing,
//      ChatUiState.Loaded(
//        List(10) { index ->
//          ChatMessage.ChatMessageText(
//            id = index.toString(),
//            sender = if (index % 2 == 0 || index > 7) ChatMessage.Sender.MEMBER else ChatMessage.Sender.HEDVIG,
//            sentAt = Clock.System.now().plus(index.seconds),
//            text = "Hello #$index" + if (index == 0) {
//              "long".repeat(15)
//            } else {
//              ""
//            },
//          )
//        }.plus(
//          ChatMessage.FailedToBeSent.ChatMessageText(
//            id = "failed",
//            sentAt = Clock.System.now(),
//            text = "Failed to be sent",
//          ),
//        ).map {
//          ChatUiState.Loaded.UiChatMessage(it, false)
//        },
//        fetchMoreMessagesUiState = ChatUiState.Loaded.FetchMoreMessagesUiState.FetchingMore,
//        bannerText = "Test",
//        haveSentAtLeastOneMessage = false,
//      ),
//    ),
//  )
