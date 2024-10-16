package com.hedvig.android.feature.chat

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.HedvigDateTimeFormatterDefaults
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.feature.chat.CbmChatUiState.Error
import com.hedvig.android.feature.chat.CbmChatUiState.Initializing
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.TopAppBarText.ClaimConversation
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.TopAppBarText.Legacy
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.TopAppBarText.NewConversation
import com.hedvig.android.feature.chat.CbmChatUiState.Loaded.TopAppBarText.ServiceConversation
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CbmChatDestination(
  viewModel: CbmChatViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (String) -> Unit,
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
    onNavigateToClaimDetails = onNavigateToClaimDetails,
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
  onNavigateToClaimDetails: (String) -> Unit,
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
    Column {
      val density = LocalDensity.current
      var topAppBarHeight by remember { mutableStateOf(0.dp) }
      ChatTopAppBar(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onNavigateToClaimDetails = onNavigateToClaimDetails,
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
            HedvigFullScreenCenterAlignedProgressDebounced()
          }

          Error -> {
            HedvigErrorSection(onRetryLoadingChat)
          }

          is Loaded -> {
            CbmChatLoadedScreen(
              uiState = uiState,
              imageLoader = imageLoader,
              appPackageId = appPackageId,
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
  onNavigateToClaimDetails: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    TopAppBarActionType.BACK,
    onNavigateUp,
    modifier,
  ) {
    Box(
      Modifier
          .then(
              if (uiState is Loaded && uiState.claimId != null) {
                  Modifier.clickable(onClick = dropUnlessResumed { onNavigateToClaimDetails(uiState.claimId) })
              } else {
                  Modifier
              },
          )
          .wrapContentHeight(Alignment.CenterVertically),
    ) {
      when (uiState) {
        is Loaded -> {
          val textStyle = HedvigTheme.typography.headlineSmall
          CompositionLocalProvider(LocalTextStyle provides textStyle) {
            when (val topAppBarText = uiState.topAppBarText) {
              Legacy -> HedvigText(stringResource(R.string.CHAT_CONVERSATION_HISTORY_TITLE))
              NewConversation -> {
                Column {
                  HedvigText(stringResource(R.string.CHAT_NEW_CONVERSATION_TITLE))
                  HedvigText(
                    stringResource(R.string.CHAT_NEW_CONVERSATION_SUBTITLE),
                    color = HedvigTheme.colorScheme.textSecondary,
                  )
                }
              }

              is ClaimConversation -> {
                Column {
                  HedvigText(topAppBarText.claimType ?: stringResource(R.string.home_claim_card_pill_claim))
                  val subtitle = chatTopAppBarFormattedSubtitle(topAppBarText.createdAt)
                  HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
                }
              }

              is ServiceConversation -> {
                Column {
                  HedvigText(stringResource(R.string.CHAT_CONVERSATION_QUESTION_TITLE))
                  val subtitle = chatTopAppBarFormattedSubtitle(topAppBarText.createdAt)
                  HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
                }
              }
            }
          }
        }

        else -> HedvigText(stringResource(R.string.CHAT_TITLE))
      }
    }
  }
}

@Composable
private fun chatTopAppBarFormattedSubtitle(createdAt: Instant): String {
  val locale = getLocale()
  val stringResource = stringResource(R.string.claim_status_detail_submitted)
  return remember(locale, stringResource, createdAt) {
    val formattedDate = HedvigDateTimeFormatterDefaults
      .monthDateAndYear(locale)
      .format(
        createdAt.toLocalDateTime(
          TimeZone.currentSystemDefault(),
        ).toJavaLocalDateTime(),
      )
    "$stringResource $formattedDate"
  }
}

@HedvigPreview
@Composable
private fun PreviewChatScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isError: Boolean,
) {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    com.hedvig.android.design.system.hedvig.HedvigTheme {
      androidx.compose.material3.Surface(color = MaterialTheme.colorScheme.background) {
        com.hedvig.android.design.system.hedvig.Surface(
          color = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.backgroundPrimary,
        ) {
          ChatScreen(
            uiState = if (isError) CbmChatUiState.Error else CbmChatUiState.Initializing,
            imageLoader = rememberPreviewImageLoader(),
            appPackageId = "",
            openUrl = {},
            onBannerLinkClicked = {},
            onNavigateToClaimDetails = {},
            onNavigateUp = {},
            onSendMessage = {},
            onSendPhoto = {},
            onSendMedia = {},
            onRetrySendChatMessage = {},
            onRetryLoadingChat = {},
          )
        }
      }
    }
  }
}
