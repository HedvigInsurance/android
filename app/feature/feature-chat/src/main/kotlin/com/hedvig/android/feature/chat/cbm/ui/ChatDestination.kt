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
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.feature.chat.ChatUiState
import com.hedvig.android.feature.chat.cbm.CbmChatEvent
import com.hedvig.android.feature.chat.cbm.CbmChatUiState
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Initializing
import com.hedvig.android.feature.chat.cbm.CbmChatUiState.Loaded
import com.hedvig.android.feature.chat.cbm.CbmChatViewModel
import com.hedvig.android.feature.chat.ui.chatScrollBehavior
import com.hedvig.android.feature.chat.ui.chatTopAppBarWindowInsets
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import hedvig.resources.R

@Composable
internal fun CbmChatDestination(
  viewModel: CbmChatViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openUrl: (String) -> Unit,
  onNavigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val onBannerLinkClicked: (String) -> Unit = { url: String ->
    if (url == hedvigDeepLinkContainer.helpCenter) {
      val haveSentAtLeastOneMessage = uiState.safeCast<ChatUiState.Loaded>()?.haveSentAtLeastOneMessage ?: false
      logAction(
        ActionType.CUSTOM,
        "Help center opened from the chat",
        mapOf("haveSentAMessage" to haveSentAtLeastOneMessage),
      )
    }
    openUrl(url)
  }
  ChatScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    openUrl = openUrl,
    onBannerLinkClicked = onBannerLinkClicked,
    onNavigateUp = onNavigateUp,
    onSendMessage = { message: String ->
      viewModel.emit(CbmChatEvent.SendTextMessage(message))
    },
    onSendPhoto = { uri: Uri ->
      logcat { "viewModel.emit(ChatEvent.SendPhotoMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      // todo cmb photo
//      viewModel.emit(CbmChatEvent.SendPhotoMessage(uri))
    },
    onSendMedia = { uri: Uri ->
      logcat { "viewModel.emit(CbmChatEvent.SendMediaMessage(uri)):${uri.path} to vm:${viewModel.hashCode()}" }
      // todo cbm media
//      viewModel.emit(CbmChatEvent.SendMediaMessage(uri))
    },
    onRetrySendChatMessage = { messageId ->
      viewModel.emit(CbmChatEvent.RetrySendChatMessage(messageId))
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
        if (uiState is CbmChatUiState.Loaded) {
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
        val shouldShowLoadingIndicator = when (uiState) {
          Initializing -> true
          is Loaded -> {
            uiState.messages.itemCount == 0 && !uiState.messages.loadState.isIdle
          }
        }
        // todo cbm check if indicator should show here
        if (shouldShowLoadingIndicator) {
          HedvigFullScreenCenterAlignedProgress()
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
