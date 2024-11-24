package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.design.system.hedvig.TopAppBarLayoutForActions
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.notificationCircle
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimStatus.CLOSED
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimstatus.ClaimStatusCardContent
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import hedvig.resources.R
import java.io.File
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun ClaimDetailsDestination(
  viewModel: ClaimDetailsViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  navigateUp: () -> Unit,
  navigateToConversation: (String) -> Unit,
  onFilesToUploadSelected: (List<Uri>, targetUploadUrl: String) -> Unit,
  openUrl: (String) -> Unit,
  sharePdf: (File) -> Unit,
) {
  val viewState by viewModel.uiState.collectAsStateWithLifecycle()
  ClaimDetailScreen(
    uiState = viewState,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    openUrl = openUrl,
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
        hasUnreadMessages = uiState.safeCast<ClaimDetailUiState.Content>()?.hasUnreadMessages ?: false,
        navigateUp = navigateUp,
        navigateToConversation = uiState.safeCast<ClaimDetailUiState.Content>()?.conversationId?.let {
          { navigateToConversation(it) }
        },
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
          ClaimDetailScreen(
            uiState = uiState,
            onDismissUploadError = onDismissUploadError,
            openUrl = openUrl,
            imageLoader = imageLoader,
            downloadFromUrl = downloadFromUrl,
            sharePdf = sharePdf,
            onDismissDownloadError = onDismissDownloadError,
            onLaunchMediaRequest = { photoPicker.launch(PickVisualMediaRequest()) },
            onPickFile = { filePicker.launch("*/*") },
            onTakePhoto = { photoCaptureState.launchTakePhotoRequest() },
          )
        }

        ClaimDetailUiState.Error -> HedvigErrorSection(onButtonClick = retry)
        ClaimDetailUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      }
    }
  }
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState.Content,
  openUrl: (String) -> Unit,
  onDismissUploadError: () -> Unit,
  imageLoader: ImageLoader,
  downloadFromUrl: (String) -> Unit,
  onDismissDownloadError: () -> Unit,
  sharePdf: (File) -> Unit,
  onLaunchMediaRequest: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
) {
  if (uiState.savedFileUri != null) {
    LaunchedEffect(uiState.savedFileUri) {
      sharePdf(uiState.savedFileUri)
    }
  }
  var showFileTypeSelectBottomSheet by remember { mutableStateOf(false) }

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
    onPickPhoto = {
      onLaunchMediaRequest()
      showFileTypeSelectBottomSheet = false
    },
    onPickFile = {
      onPickFile()
      showFileTypeSelectBottomSheet = false
    },
    onTakePhoto = {
      onTakePhoto()
      showFileTypeSelectBottomSheet = false
    },
    onDismiss = {
      showFileTypeSelectBottomSheet = false
    },
    isVisible = showFileTypeSelectBottomSheet,
  )
  Column {
    DynamicFilesGridBetweenOtherThings(
      belowGridContent = {
        AfterGridContent(
          uiState = uiState,
          onAddFilesButtonClick = { showFileTypeSelectBottomSheet = true },
          onDismissUploadError = onDismissUploadError,
        )
      },
      aboveGridContent = {
        BeforeGridContent(
          downloadFromUrl = downloadFromUrl,
          uiState = uiState,
        )
      },
      files = uiState.files,
      imageLoader = imageLoader,
      onClickFile = { fileId ->
        val url = uiState.files.firstOrNull { it.id == fileId }?.url
        if (url != null) {
          openUrl(url)
        }
      },
      onRemoveFile = null,
      contentPadding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
        .only(
          WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
        ).asPaddingValues(),
    )
  }
}

@Composable
private fun ClaimDetailTopAppBar(
  hasUnreadMessages: Boolean,
  navigateUp: () -> Unit,
  navigateToConversation: (() -> Unit)?,
) {
  TopAppBar(
    title = stringResource(R.string.CLAIMS_YOUR_CLAIM),
    actionType = BACK,
    onActionClick = navigateUp,
    topAppBarActions = {
      if (navigateToConversation != null) {
        TopAppBarLayoutForActions(contentPadding = PaddingValues()) {
          IconButton(navigateToConversation, Modifier.size(40.dp)) {
            Icon(
              imageVector = HedvigIcons.Chat,
              contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
              tint = HedvigTheme.colorScheme.signalGreyElement,
              modifier = Modifier
                .size(32.dp)
                .notificationCircle(hasUnreadMessages)
                .clip(CircleShape),
            )
          }
        }
      }
    },
  )
}

@Composable
private fun BeforeGridContent(uiState: ClaimDetailUiState.Content, downloadFromUrl: (url: String) -> Unit) {
  Column {
    Spacer(Modifier.height(8.dp))
    HedvigCard {
      Column {
        ClaimStatusCardContent(uiState = uiState.claimStatusCardUiState, withInfoIcon = false, Modifier.padding(16.dp))
        val claimIsInUndeterminedState = uiState.claimStatus == CLOSED && uiState.claimOutcome == UNKNOWN
        if (!claimIsInUndeterminedState) {
          HorizontalDivider()
          Column(
            Modifier.padding(
              start = 18.dp,
              end = 18.dp,
              top = 16.dp,
              bottom = 18.dp,
            ),
          ) {
            HedvigText(
              text = stringResource(R.string.claim_status_title),
              style = HedvigTheme.typography.label,
            )
            HedvigText(
              text = statusParagraphText(uiState.claimStatus, uiState.claimOutcome),
              style = HedvigTheme.typography.label.copy(
                color = HedvigTheme.colorScheme.textSecondary,
              ),
            )
          }
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigText(
      stringResource(R.string.claim_status_claim_details_title),
      Modifier.padding(horizontal = 2.dp),
    )
    Spacer(Modifier.height(8.dp))
    ClaimTypeAndDatesSection(
      claimType = uiState.claimType,
      submitDate = uiState.submittedAt.date,
      incidentDate = uiState.incidentDate,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
    )
    if (uiState.termsConditionsUrl != null) {
      Spacer(Modifier.height(16.dp))
      TermsConditionsCard(
        onClick = { downloadFromUrl(uiState.termsConditionsUrl) },
        modifier = Modifier.padding(16.dp),
        isLoading = uiState.isLoadingPdf,
      )
    }
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
}

@Composable
private fun AfterGridContent(
  uiState: ClaimDetailUiState.Content,
  onAddFilesButtonClick: () -> Unit,
  onDismissUploadError: () -> Unit,
) {
  Column {
    Spacer(Modifier.height(32.dp))
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
    Spacer(Modifier.height(32.dp))
    if (uiState.uploadError != null) {
      ErrorDialog(
        title = stringResource(R.string.something_went_wrong),
        message = uiState.uploadError,
        onDismiss = onDismissUploadError,
      )
    }
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
              subtitle = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS_SUBTITLE),
            )
          },
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
          ) {
            HedvigCircularProgressIndicator()
          }
        }
      } else {
        DocumentCard(
          title = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS),
          subtitle = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS_SUBTITLE),
        )
      }
    }
  }
}

@Composable
private fun DocumentCard(title: String, subtitle: String?) {
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
          if (!subtitle.isNullOrBlank()) {
            HedvigText(
              text = subtitle,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = HedvigIcons.ArrowNorthEast,
            contentDescription = null,
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
    ClaimDetailUiState.Content.ClaimOutcome.PAID -> stringResource(R.string.claim_status_paid_support_text)
    ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED -> {
      stringResource(R.string.claim_status_not_compensated_support_text)
    }

    ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED -> {
      stringResource(R.string.claim_status_not_covered_support_text)
    }

    ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN -> ""
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
private fun ClaimTypeAndDatesSection(
  claimType: String?,
  submitDate: LocalDate?,
  incidentDate: LocalDate?,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
    Column(modifier) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            text = stringResource(R.string.claim_status_claim_details_type),
          )
        },
        endSlot = {
          HedvigText(
            text = claimType ?: stringResource(R.string.claim_casetype_insurance_case),
            textAlign = TextAlign.End,
          )
        },
      )
      if (incidentDate != null) {
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(
              text = stringResource(R.string.claim_status_claim_details_incident_date),
            )
          },
          endSlot = {
            HedvigText(
              text = dateTimeFormatter.format(incidentDate.toJavaLocalDate()),
              textAlign = TextAlign.End,
            )
          },
        )
      }

      if (submitDate != null) {
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(
              text = stringResource(R.string.claim_status_claim_details_submitted),
            )
          },
          endSlot = {
            HedvigText(
              text = dateTimeFormatter.format(submitDate.toJavaLocalDate()),
              textAlign = TextAlign.End,
            )
          },
        )
      }
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewClaimDetailScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimDetailScreen(
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
          isUploadingFile = false,
          uploadUri = "",
          uploadError = null,
          claimType = "Theft",
          incidentDate = LocalDate(2023, 1, 2),
          submittedAt = LocalDateTime(2023, 1, 5, 12, 35),
          insuranceDisplayName = "Home insurance",
          termsConditionsUrl = "url",
          savedFileUri = null,
          downloadError = null,
          isLoadingPdf = false,
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
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimDetailTopAppBar(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withNotification: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimDetailTopAppBar(
        withNotification,
        {},
        {},
      )
    }
  }
}
