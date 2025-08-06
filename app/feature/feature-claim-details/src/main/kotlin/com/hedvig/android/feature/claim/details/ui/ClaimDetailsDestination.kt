package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Date
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.DateTime
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Text
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.File
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.notificationCircle
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.logger.logcat
import com.hedvig.android.shared.file.upload.ui.FilePickerBottomSheet
import com.hedvig.android.ui.claimstatus.ClaimStatusCard
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import hedvig.resources.R
import java.io.File
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime

@Composable
internal fun ClaimDetailsDestination(
  viewModel: ClaimDetailsViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  navigateUp: () -> Unit,
  navigateToConversation: (String) -> Unit,
  onFilesToUploadSelected: (List<Uri>, targetUploadUrl: String) -> Unit,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  sharePdf: (File) -> Unit,
) {
  val viewState by viewModel.uiState.collectAsStateWithLifecycle()
  ClaimDetailScreen(
    uiState = viewState,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    onDismissUploadError = { viewModel.emit(ClaimDetailsEvent.DismissUploadError) },
    retry = { viewModel.emit(ClaimDetailsEvent.Retry) },
    navigateUp = navigateUp,
    navigateToConversation = navigateToConversation,
    onFilesToUploadSelected = onFilesToUploadSelected,
    downloadFromUrl = { viewModel.emit(ClaimDetailsEvent.DownloadPdf(it)) },
    sharePdf = {
      viewModel.emit(ClaimDetailsEvent.HandledSharingPdfFile)
      sharePdf(it)
    },
    onDismissDownloadError = { viewModel.emit(ClaimDetailsEvent.DismissDownloadError) },
  )
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState,
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onDismissUploadError: () -> Unit,
  retry: () -> Unit,
  navigateUp: () -> Unit,
  navigateToConversation: (String) -> Unit,
  onFilesToUploadSelected: (files: List<Uri>, uploadUri: String) -> Unit,
  downloadFromUrl: (String) -> Unit,
  sharePdf: (File) -> Unit,
  onDismissDownloadError: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      ClaimDetailTopAppBar(
        navigateUp = navigateUp,
      )
      when (uiState) {
        is ClaimDetailUiState.Content -> {
          val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
            logcat { "ChatFileState sending photoCaptureState uri:$uri" }
            onFilesToUploadSelected(listOf(uri), uiState.uploadUri)
          }
          val photoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
          ) { resultingUriList: List<Uri> ->
            logcat { "ChatFileState sending photoPicker uris:$resultingUriList" }
            onFilesToUploadSelected(resultingUriList, uiState.uploadUri)
          }
          val filePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
          ) { resultingUriList: List<Uri> ->
            logcat { "ChatFileState sending filePicker uris:$resultingUriList" }
            onFilesToUploadSelected(resultingUriList, uiState.uploadUri)
          }
          ClaimDetailContentScreen(
            uiState = uiState,
            onDismissUploadError = onDismissUploadError,
            openUrl = openUrl,
            onNavigateToImageViewer = onNavigateToImageViewer,
            imageLoader = imageLoader,
            downloadFromUrl = downloadFromUrl,
            sharePdf = sharePdf,
            onDismissDownloadError = onDismissDownloadError,
            onLaunchMediaRequest = { photoPicker.launch(PickVisualMediaRequest()) },
            onPickFile = { filePicker.launch("*/*") },
            onTakePhoto = { photoCaptureState.launchTakePhotoRequest() },
            hasUnreadMessages = uiState.safeCast<ClaimDetailUiState.Content>()?.hasUnreadMessages == true,
            navigateToConversation = uiState.safeCast<ClaimDetailUiState.Content>()?.conversationId?.let {
              { navigateToConversation(it) }
            },
          )
        }

        ClaimDetailUiState.Error -> {
          Spacer(Modifier.weight(1f))
          HedvigErrorSection(onButtonClick = retry)
          Spacer(Modifier.weight(1f))
        }

        ClaimDetailUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      }
    }
  }
}

@Composable
private fun ClaimDetailContentScreen(
  uiState: ClaimDetailUiState.Content,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onDismissUploadError: () -> Unit,
  imageLoader: ImageLoader,
  downloadFromUrl: (String) -> Unit,
  onDismissDownloadError: () -> Unit,
  sharePdf: (File) -> Unit,
  onLaunchMediaRequest: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
  hasUnreadMessages: Boolean,
  navigateToConversation: (() -> Unit)?,
) {
  if (uiState.savedFileUri != null) {
    LaunchedEffect(uiState.savedFileUri) {
      sharePdf(uiState.savedFileUri)
    }
  }
  val fileTypeSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()

  if (uiState.downloadError == true) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = stringResource(id = R.string.travel_certificate_downloading_error),
      onDismiss = {
        onDismissDownloadError()
      },
    )
  }
  FilePickerBottomSheet(
    sheetState = fileTypeSelectBottomSheetState,
    onPickPhoto = {
      onLaunchMediaRequest()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onPickFile = {
      onPickFile()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onTakePhoto = {
      onTakePhoto()
      fileTypeSelectBottomSheetState.dismiss()
    },
  )
  NonDynamicGrid(
    uiState = uiState,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    onDismissUploadError = onDismissUploadError,
    imageLoader = imageLoader,
    downloadFromUrl = downloadFromUrl,
    hasUnreadMessages = hasUnreadMessages,
    navigateToConversation = navigateToConversation,
    onAddFilesButtonClick = fileTypeSelectBottomSheetState::show,
  )
}

@Composable
private fun ClaimDetailTopAppBar(navigateUp: () -> Unit) {
  TopAppBar(
    title = stringResource(R.string.CLAIMS_YOUR_CLAIM),
    actionType = BACK,
    onActionClick = navigateUp,
  )
}

@Composable
private fun NonDynamicGrid(
  uiState: ClaimDetailUiState.Content,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onDismissUploadError: () -> Unit,
  imageLoader: ImageLoader,
  downloadFromUrl: (String) -> Unit,
  hasUnreadMessages: Boolean,
  onAddFilesButtonClick: () -> Unit,
  navigateToConversation: (() -> Unit)?,
) {
  val explanationBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  ExplanationBottomSheet(explanationBottomSheetState)
  Column(
    Modifier
      .padding(
        PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
          .only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
          ).asPaddingValues(),
      )
      .verticalScroll(rememberScrollState()),
  ) {
    BeforeGridContent(
      uiState = uiState,
      hasUnreadMessages = hasUnreadMessages,
      navigateToConversation = navigateToConversation,
      onExplanationButtonClick = { explanationBottomSheetState.show(Unit) },
    )
    if (uiState.files.isNotEmpty()) {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(109.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp),
        modifier = Modifier.height(109.dp),
      ) {
        items(
          items = uiState.files,
          key = { it.id },
        ) { uiFile ->
          File(
            id = uiFile.id,
            name = uiFile.name,
            path = uiFile.localPath ?: uiFile.url,
            mimeType = uiFile.mimeType,
            imageLoader = imageLoader,
            onRemoveFile = null,
            onClickFile = { fileId ->
              val url = uiState.files.firstOrNull { it.id == fileId }?.url
              if (url != null) {
                openUrl(url)
              }
            },
            onNavigateToImageViewer = onNavigateToImageViewer,
          )
        }
      }
    }
    AfterGridContent(
      uiState = uiState,
      onAddFilesButtonClick = onAddFilesButtonClick,
      onDismissUploadError = onDismissUploadError,
      downloadFromUrl = downloadFromUrl,
    )
  }
}

@Composable
internal fun ExplanationBottomSheet(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(sheetState) { _ ->
    HedvigText(
      text = stringResource(id = R.string.claim_status_claim_details_info_text),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      buttonSize = Large,
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun BeforeGridContent(
  uiState: ClaimDetailUiState.Content,
  hasUnreadMessages: Boolean,
  navigateToConversation: (() -> Unit)?,
  onExplanationButtonClick: () -> Unit,
) {
  Column {
    Spacer(Modifier.height(8.dp))
    if (uiState.infoText != null) {
      HedvigNotificationCard(
        modifier = Modifier.fillMaxWidth(),
        message = uiState.infoText,
        priority = NotificationDefaults.NotificationPriority.Info,
      )
      Spacer(Modifier.height(8.dp))
    }
    ClaimStatusCard(uiState = uiState.claimStatusCardUiState)
    if (navigateToConversation != null || !uiState.claimIsInUndeterminedState) {
      Spacer(Modifier.height(8.dp))
      HedvigCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          if (!uiState.claimIsInUndeterminedState) {
            HedvigText(
              text = statusParagraphText(uiState.claimStatus, uiState.claimOutcome),
              style = HedvigTheme.typography.bodySmall,
            )
          }
          if (navigateToConversation != null && !uiState.claimIsInUndeterminedState) {
            HorizontalDivider()
          }
          if (navigateToConversation != null) {
            HorizontalItemsWithMaximumSpaceTaken(
              modifier = Modifier
                .clip(HedvigTheme.shapes.cornerXSmall)
                .clickable(onClick = navigateToConversation),
              startSlot = {
                HedvigText(
                  text = stringResource(R.string.claim_status_detail_message_view_body),
                  style = HedvigTheme.typography.bodySmall,
                  modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                )
              },
              endSlot = {
                IconButton(
                  onClick = navigateToConversation,
                  modifier = Modifier
                    .size(40.dp)
                    .wrapContentSize(Alignment.CenterEnd),
                ) {
                  Icon(
                    imageVector = HedvigIcons.Chat,
                    contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
                    tint = HedvigTheme.colorScheme.signalGreyElement,
                    modifier = Modifier
                      .size(32.dp)
                      .notificationCircle(hasUnreadMessages),
                  )
                }
              },
              spaceBetween = 8.dp,
            )
          }
        }
      }
    }
  }
  Spacer(Modifier.height(24.dp))
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          stringResource(R.string.claim_status_claim_details_title),
          Modifier.padding(horizontal = 2.dp),
        )
      }
    },
    endSlot = {
      Row(
        horizontalArrangement = Arrangement.End,
      ) {
        IconButton(
          onClick = onExplanationButtonClick,
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            imageVector = HedvigIcons.InfoFilled,
            contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
            modifier = Modifier.size(24.dp),
          )
        }
      }
    },
    spaceBetween = 8.dp,
  )

  Spacer(Modifier.height(8.dp))
  DisplayItemsSection(
    displayItems = uiState.displayItems,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 2.dp),
  )
  Spacer(Modifier.height(24.dp))
  HedvigText(
    stringResource(R.string.claim_status_detail_uploaded_files_info_title),
    Modifier.padding(horizontal = 2.dp),
  )
  Spacer(Modifier.height(8.dp))
  when (uiState.submittedContent) {
    is ClaimDetailUiState.Content.SubmittedContent.Audio -> {
      ClaimDetailHedvigAudioPlayerItem(uiState.submittedContent.signedAudioURL)
    }

    is ClaimDetailUiState.Content.SubmittedContent.FreeText -> {
      HedvigCard(Modifier.fillMaxWidth()) {
        HedvigText(
          uiState.submittedContent.text,
          Modifier.padding(16.dp),
        )
      }
    }

    else -> {}
  }
  Spacer(Modifier.height(8.dp))
}

@Composable
private fun AfterGridContent(
  uiState: ClaimDetailUiState.Content,
  onAddFilesButtonClick: () -> Unit,
  onDismissUploadError: () -> Unit,
  downloadFromUrl: (url: String) -> Unit,
) {
  Column {
    if (uiState.isUploadingFilesEnabled) {
      Spacer(Modifier.height(24.dp))
      HedvigText(
        text = stringResource(id = R.string.claim_status_uploaded_files_upload_text),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      val text = if (uiState.files.isNotEmpty()) {
        stringResource(id = R.string.claim_status_detail_add_more_files)
      } else {
        stringResource(id = R.string.claim_status_detail_add_files)
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
      ) {
        HedvigButton(
          text = text,
          buttonSize = Medium,
          onClick = onAddFilesButtonClick,
          enabled = true,
          isLoading = uiState.isUploadingFile,
        )
      }
      Spacer(Modifier.height(24.dp))
    }
    if (uiState.uploadError != null) {
      ErrorDialog(
        title = stringResource(R.string.something_went_wrong),
        message = uiState.uploadError,
        onDismiss = onDismissUploadError,
      )
    }
    if (uiState.termsConditionsUrl != null || uiState.appealInstructionsUrl != null) {
      Spacer(Modifier.height(16.dp))
      HedvigText(
        stringResource(R.string.claim_status_detail_documents_title),
        Modifier.padding(horizontal = 2.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    if (uiState.termsConditionsUrl != null) {
      TermsConditionsCard(
        onClick = { downloadFromUrl(uiState.termsConditionsUrl) },
        modifier = Modifier.padding(16.dp),
        isLoading = uiState.termsConditionsUrl == uiState.isLoadingPdf,
      )
      Spacer(Modifier.height(8.dp))
    }
    if (uiState.appealInstructionsUrl != null) {
      AppealInstructionCard(
        onClick = { downloadFromUrl(uiState.appealInstructionsUrl) },
        modifier = Modifier.padding(16.dp),
        isLoading = uiState.appealInstructionsUrl == uiState.isLoadingPdf,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TermsConditionsCard(onClick: () -> Unit, isLoading: Boolean, modifier: Modifier = Modifier) {
  HedvigCard(onClick = onClick) {
    Row(
      modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isLoading) {
        LayoutWithoutPlacement(
          sizeAdjustingContent = {
            DocumentCard(
              title = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS),
            )
          },
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
          ) {
            HedvigThreeDotsProgressIndicator()
          }
        }
      } else {
        DocumentCard(
          title = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS),
        )
      }
    }
  }
}

@Composable
private fun AppealInstructionCard(onClick: () -> Unit, isLoading: Boolean, modifier: Modifier = Modifier) {
  HedvigCard(onClick = onClick) {
    Row(
      modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isLoading) {
        LayoutWithoutPlacement(
          sizeAdjustingContent = {
            DocumentCard(
              title = stringResource(id = R.string.claim_status_appeal_instruction_link_text),
            )
          },
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
          ) {
            HedvigThreeDotsProgressIndicator()
          }
        }
      } else {
        DocumentCard(
          title = stringResource(id = R.string.claim_status_appeal_instruction_link_text),
        )
      }
    }
  }
}

@Composable
private fun DocumentCard(title: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Column {
          HedvigText(
            text = stringWithShiftedLabel(
              text = title,
              labelText = "PDF",
              labelFontSize = HedvigTheme.typography.label.fontSize,
              textColor = LocalContentColor.current,
              textFontSize = LocalTextStyle.current.fontSize,
            ),
          )
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = HedvigIcons.ArrowNorthEast,
            contentDescription = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
            modifier = Modifier.size(16.dp),
          )
        }
      },
      spaceBetween = 8.dp,
    )
  }
}

@Composable
private fun statusParagraphText(
  claimStatus: ClaimDetailUiState.Content.ClaimStatus,
  claimOutcome: ClaimDetailUiState.Content.ClaimOutcome,
): String = when (claimStatus) {
  ClaimDetailUiState.Content.ClaimStatus.CREATED -> stringResource(R.string.claim_status_submitted_support_text)
  ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS -> stringResource(R.string.claim_status_being_handled_support_text)
  ClaimDetailUiState.Content.ClaimStatus.CLOSED -> when (claimOutcome) {
    ClaimDetailUiState.Content.ClaimOutcome.PAID -> stringResource(R.string.claim_status_paid_support_text_short)
    ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED -> {
      stringResource(R.string.claim_status_not_compensated_support_text)
    }

    ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED -> {
      stringResource(R.string.claim_status_not_covered_support_text)
    }

    ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN -> ""
    ClaimDetailUiState.Content.ClaimOutcome.UNRESPONSIVE -> stringResource(
      R.string.claim_outcome_unresponsive_support_text,
    )
  }

  ClaimDetailUiState.Content.ClaimStatus.REOPENED -> {
    stringResource(R.string.claim_status_being_handled_reopened_support_text)
  }

  ClaimDetailUiState.Content.ClaimStatus.UNKNOWN -> stringResource(R.string.claim_status_being_handled_support_text)
}

@Composable
private fun ClaimDetailHedvigAudioPlayerItem(signedAudioUrl: SignedAudioUrl, modifier: Modifier = Modifier) {
  Column(modifier) {
    val audioPlayer = rememberAudioPlayer(playableAudioSource = PlayableAudioSource.RemoteUrl(signedAudioUrl))
    HedvigAudioPlayer(audioPlayer = audioPlayer)
  }
}

@Composable
private fun DisplayItemsSection(displayItems: List<DisplayItem>, modifier: Modifier = Modifier) {
  CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
    Column(modifier) {
      for (displayItem in displayItems) {
        HorizontalItemsWithMaximumSpaceTaken(
          spaceBetween = 8.dp,
          startSlot = {
            HedvigText(text = displayItem.title)
          },
          endSlot = {
            val formatter = rememberHedvigDateTimeFormatter()
            HedvigText(
              text = when (val item = displayItem.value) {
                is Date -> formatter.format(item.date.toJavaLocalDate())
                is DateTime -> formatter.format(item.localDateTime.toJavaLocalDateTime())
                is Text -> item.text
              },
              textAlign = TextAlign.End,
            )
          },
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimDetailScreen(
  @PreviewParameter(
    ClaimDetailUiStateProvider::class,
  ) uiState: ClaimDetailUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimDetailScreen(
        uiState = uiState,
        openUrl = {},
        imageLoader = rememberPreviewImageLoader(),
        onDismissUploadError = {},
        downloadFromUrl = {},
        sharePdf = {},
        onDismissDownloadError = {},
        onNavigateToImageViewer = { _, _ -> },
        appPackageId = "",
        retry = { },
        navigateUp = {},
        navigateToConversation = {},
        onFilesToUploadSelected = { _, _ -> },
      )
    }
  }
}

private class ClaimDetailUiStateProvider :
  CollectionPreviewParameterProvider<ClaimDetailUiState>(
    listOf(
      ClaimDetailUiState.Loading,
      ClaimDetailUiState.Error,
    ),
  )

@HedvigMultiScreenPreview
@Composable
private fun PreviewClaimDetailScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withNotification: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimDetailContentScreen(
        uiState = ClaimDetailUiState.Content(
          claimId = "id",
          conversationId = "idd",
          hasUnreadMessages = true,
          submittedContent = ClaimDetailUiState.Content.SubmittedContent.FreeText("Some free input text"),
          claimStatusCardUiState = ClaimStatusCardUiState(
            id = "id",
            claimType = "Broken item",
            insuranceDisplayName = null, // "Home Insurance Homeowner",
            submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
            pillTypes = listOf(
              ClaimPillType.Claim,
              ClaimPillType.Closed.GenericClosed,
              ClaimPillType.Closed.Paid,
              ClaimPillType.PaymentAmount(UiMoney(399.0, UiCurrencyCode.SEK)),
              ClaimPillType.Closed.NotCompensated,
              ClaimPillType.Closed.NotCovered,
            ),
            claimProgressItemsUiState = listOf(
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.Submitted,
                ClaimProgressSegment.SegmentType.ACTIVE,
              ),
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.BeingHandled,
                ClaimProgressSegment.SegmentType.ACTIVE,
              ),
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.Closed,
                ClaimProgressSegment.SegmentType.ACTIVE,
              ),
            ),
          ),
          claimStatus = ClaimDetailUiState.Content.ClaimStatus.CLOSED,
          claimOutcome = ClaimDetailUiState.Content.ClaimOutcome.PAID,
          files = List(6) { index ->
            UiFile(
              id = index.toString(),
              name = "test#$index".let { if (index == 2) it.repeat(10) else it },
              mimeType = "",
              url = index.toString(),
              localPath = null,
            )
          },
          // files = listOf(),
          isUploadingFile = false,
          uploadUri = "",
          uploadError = null,
          claimType = "Theft",
          submittedAt = LocalDateTime(2023, 1, 5, 12, 35),
          insuranceDisplayName = "Home insurance",
          termsConditionsUrl = "url",
          savedFileUri = null,
          downloadError = null,
          isLoadingPdf = null,
          appealInstructionsUrl = "dd",
          isUploadingFilesEnabled = false,
          infoText = "If you have more receipts related to this claim, you can upload more on this page.",
          displayItems = listOf(
            DisplayItem("Type", DisplayItem.DisplayItemValue.Text("Respiratory disorder")),
            DisplayItem("Submitted", DisplayItem.DisplayItemValue.Text("2025-02-03")),
          ),
        ),
        openUrl = {},
        imageLoader = rememberPreviewImageLoader(),
        onDismissUploadError = {},
        downloadFromUrl = {},
        sharePdf = {},
        onDismissDownloadError = {},
        onLaunchMediaRequest = {},
        onTakePhoto = {},
        onPickFile = {},
        onNavigateToImageViewer = { _, _ -> },
        hasUnreadMessages = withNotification,
        navigateToConversation = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimDetailTopAppBar() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimDetailTopAppBar(
        {},
      )
    }
  }
}
