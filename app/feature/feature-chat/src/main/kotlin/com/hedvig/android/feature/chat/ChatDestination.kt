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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.SimpleCache
import coil.ImageLoader
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.rememberPreviewSimpleCache
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
  simpleVideoCache: SimpleCache,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToClaimDetails: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onNavigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChatScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    openUrl = openUrl,
    onNavigateUp = onNavigateUp,
    onNavigateToClaimDetails = onNavigateToClaimDetails,
    onNavigateToImageViewer = onNavigateToImageViewer,
    onSendMessage = { message: String -> viewModel.emit(CbmChatEvent.SendTextMessage(message)) },
    onSendPhoto = { uris: List<Uri> ->
      logcat { "viewModel.emit(ChatEvent.SendPhotoMessage(uriList)):$uris to vm:${viewModel.hashCode()}" }
      viewModel.emit(CbmChatEvent.SendPhotoMessage(uris))
    },
    onSendMedia = { uris: List<Uri> ->
      logcat { "viewModel.emit(CbmChatEvent.SendMediaMessage(uriList)):$uris to vm:${viewModel.hashCode()}" }
      viewModel.emit(CbmChatEvent.SendMediaMessage(uris))
    },
    onRetrySendChatMessage = { messageId -> viewModel.emit(CbmChatEvent.RetrySendChatMessage(messageId)) },
    onRetryLoadingChat = { viewModel.emit(CbmChatEvent.RetryLoadingChat) },
    simpleVideoCache = simpleVideoCache,
    showedFileTooBigError = { viewModel.emit(CbmChatEvent.ClearFileTooBigToast) },
    showedFileFailedToBeSentToast = { viewModel.emit(CbmChatEvent.ClearFileFailedToBeSentToast) },
    onCloseBannerClick = {
      viewModel.emit(CbmChatEvent.HideBanner)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatScreen(
  uiState: CbmChatUiState,
  imageLoader: ImageLoader,
  simpleVideoCache: Cache,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateToClaimDetails: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onSendMessage: (String) -> Unit,
  onSendPhoto: (List<Uri>) -> Unit,
  onSendMedia: (List<Uri>) -> Unit,
  onRetrySendChatMessage: (messageId: String) -> Unit,
  onRetryLoadingChat: () -> Unit,
  showedFileTooBigError: () -> Unit,
  onCloseBannerClick: () -> Unit,
  showedFileFailedToBeSentToast: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
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
              onNavigateToImageViewer = onNavigateToImageViewer,
              onRetrySendChatMessage = onRetrySendChatMessage,
              onSendMessage = onSendMessage,
              onSendPhoto = onSendPhoto,
              onSendMedia = onSendMedia,
              simpleVideoCache = simpleVideoCache,
              errorSnackbarState = if (uiState.showFileTooBigErrorToast) {
                ErrorSnackbarState(
                  messageText = stringResource(R.string.CHAT_FILE_SIZE_TOO_BIG_ERROR),
                  error = true,
                  showedError = showedFileTooBigError,
                )
              } else if (uiState.showFileFailedToBeSentToast) {
                ErrorSnackbarState(
                  messageText = stringResource(R.string.CHAT_FAILED_TO_SEND),
                  error = true,
                  showedError = showedFileFailedToBeSentToast,
                )
              } else {
                null
              },
              onCloseBannerClick = onCloseBannerClick,
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
      val headingModifier = Modifier.semantics(mergeDescendants = true) {
        heading()
      }
      when (uiState) {
        is Loaded -> {
          when (val topAppBarText = uiState.topAppBarText) {
            Legacy -> HedvigText(
              stringResource(R.string.CHAT_CONVERSATION_HISTORY_TITLE),
              modifier = headingModifier,
            )
            NewConversation -> {
              Column(modifier = headingModifier) {
                HedvigText(stringResource(R.string.CHAT_NEW_CONVERSATION_TITLE))
                HedvigText(
                  stringResource(R.string.CHAT_NEW_CONVERSATION_SUBTITLE),
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              }
            }

            is ClaimConversation -> {
              Column(modifier = headingModifier) {
                HedvigText(topAppBarText.claimType ?: stringResource(R.string.home_claim_card_pill_claim))
                val subtitle = chatTopAppBarFormattedSubtitle(topAppBarText.createdAt)
                HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
              }
            }

            is ServiceConversation -> {
              Column(modifier = headingModifier) {
                HedvigText(stringResource(R.string.CHAT_CONVERSATION_QUESTION_TITLE))
                val subtitle = chatTopAppBarFormattedSubtitle(topAppBarText.createdAt)
                HedvigText(subtitle, color = HedvigTheme.colorScheme.textSecondary)
              }
            }
          }
        }

        else -> HedvigText(stringResource(R.string.CHAT_TITLE), modifier = headingModifier)
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
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ChatScreen(
        uiState = if (isError) Error else Initializing,
        imageLoader = rememberPreviewImageLoader(),
        appPackageId = "",
        openUrl = {},
        onNavigateToImageViewer = { _, _ -> },
        onNavigateToClaimDetails = {},
        onNavigateUp = {},
        onSendMessage = {},
        onSendPhoto = {},
        onSendMedia = {},
        onRetrySendChatMessage = {},
        onRetryLoadingChat = {},
        simpleVideoCache = rememberPreviewSimpleCache(),
        showedFileTooBigError = {},
        showedFileFailedToBeSentToast = {},
        onCloseBannerClick = {},
      )
    }
  }
}
