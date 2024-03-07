package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.ui.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimstatus.ClaimStatusCard
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.AudioPlayerState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import hedvig.resources.R
import java.io.File
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode

@Composable
internal fun ClaimDetailsDestination(
  viewModel: ClaimDetailsViewModel,
  imageLoader: ImageLoader,
  appPackageId: String,
  navigateUp: () -> Unit,
  onChatClick: () -> Unit,
  onUri: (List<Uri>, targetUploadUrl: String) -> Unit,
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
    onChatClick = onChatClick,
    onUri = onUri,
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
  onChatClick: () -> Unit,
  onUri: (files: List<Uri>, uploadUri: String) -> Unit,
  downloadFromUrl: (String) -> Unit,
  sharePdf: (File) -> Unit,
  onDismissDownloadError: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        onClick = navigateUp,
        title = stringResource(R.string.CLAIMS_YOUR_CLAIM),
      )
      when (uiState) {
        is ClaimDetailUiState.Content -> {
          val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
            logcat { "ChatFileState sending uri:$uri" }
            onUri(listOf(uri), uiState.uploadUri)
          }
          val photoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
          ) { resultingUriList: List<Uri> ->
            onUri(resultingUriList, uiState.uploadUri)
          }
          val filePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
          ) { resultingUriList: List<Uri> ->
            onUri(resultingUriList, uiState.uploadUri)
          }
          ClaimDetailScreen(
            uiState = uiState,
            onChatClick = onChatClick,
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

        ClaimDetailUiState.Error -> HedvigErrorSection(retry = retry)
        ClaimDetailUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      }
    }
  }
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState.Content,
  onChatClick: () -> Unit,
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
  if (showFileTypeSelectBottomSheet) {
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
    )
  }
  Column {
    DynamicFilesGridBetweenOtherThings(
      bottomSpacing = { AfterGridSpacing() },
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
          onChatClick = onChatClick,
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
      gridContentPaddingValues = PaddingValues(),
      modifier = Modifier.padding(
        PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing.only(
          WindowInsetsSides.Horizontal,
        )
          .asPaddingValues(),
      ),
    )
  }
}

@Composable
private fun BeforeGridContent(
  uiState: ClaimDetailUiState.Content,
  onChatClick: () -> Unit,
  downloadFromUrl: (url: String) -> Unit,
) {
  Column {
    Spacer(Modifier.height(8.dp))
    ClaimStatusCard(
      uiState = uiState.claimStatusCardUiState,
      onClick = null,
      claimType = uiState.claimType,
      insuranceDisplayName = uiState.insuranceDisplayName,
    )
    Spacer(Modifier.height(8.dp))
    ClaimInfoCard(uiState.claimStatus, uiState.claimOutcome, onChatClick)
    Spacer(Modifier.height(24.dp))
    Text(
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
    Text(
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
          Text(
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
    Text(
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
      HedvigContainedSmallButton(
        text = text,
        onClick = onAddFilesButtonClick,
        isLoading = uiState.isUploadingFile,
      )
    }
    if (uiState.uploadError != null) {
      ErrorDialog(
        message = uiState.uploadError,
        onDismiss = onDismissUploadError,
      )
    }
  }
}

@Composable
private fun AfterGridSpacing() {
  Spacer(Modifier.height(32.dp))
  Spacer(
    Modifier.windowInsetsPadding(
      WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal +
          WindowInsetsSides.Bottom,
      ),
    ),
  )
}

@Composable
private fun TermsConditionsCard(onClick: () -> Unit, isLoading: Boolean, modifier: Modifier = Modifier) {
  HedvigCard(onClick) {
    Row(
      modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isLoading) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          CircularProgressIndicator()
        }
      } else {
        Column {
          val fontSize = MaterialTheme.typography.bodySmall.fontSize
          Text(
            text = buildAnnotatedString {
              append(stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS))
              withStyle(SpanStyle(baselineShift = BaselineShift(0.3f), fontSize = fontSize)) {
                append("PDF")
              }
            },
          )
          Text(
            text = stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_TERMS_SUBTITLE),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Hedvig.ArrowNorthEast, contentDescription = null)
      }
    }
  }
}

@Composable
internal fun ClaimInfoCard(
  claimStatus: ClaimDetailUiState.Content.ClaimStatus,
  claimOutcome: ClaimDetailUiState.Content.ClaimOutcome,
  onChatClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier) {
    Column {
      val claimIsInUndeterminedState = claimStatus == ClaimDetailUiState.Content.ClaimStatus.CLOSED &&
        claimOutcome == ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
      if (!claimIsInUndeterminedState) {
        Text(
          text = statusParagraphText(claimStatus, claimOutcome),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = stringResource(R.string.claim_status_contact_generic_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = stringResource(R.string.claim_status_contact_generic_title),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onChatClick) {
          Image(
            imageVector = Icons.Hedvig.Chat,
            contentDescription = stringResource(R.string.claim_status_detail_chat_button_description),
            modifier = Modifier.size(32.dp),
          )
        }
      }
    }
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
    Spacer(Modifier.height(8.dp))
    val audioPlayerState by audioPlayer.audioPlayerState.collectAsStateWithLifecycle()
    AnimatedVisibility(visible = audioPlayerState !is AudioPlayerState.Failed) {
      Text(
        text = stringResource(R.string.claim_status_files_claim_audio_footer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
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
  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
    Column(modifier) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Text(
            text = stringResource(R.string.claim_status_claim_details_type),
          )
        },
        endSlot = {
          Text(
            text = claimType ?: stringResource(R.string.claim_casetype_insurance_case),
            textAlign = TextAlign.End,
          )
        },
      )
      if (incidentDate != null) {
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            Text(
              text = stringResource(R.string.claim_status_claim_details_incident_date),
            )
          },
          endSlot = {
            Text(
              text = dateTimeFormatter.format(incidentDate.toJavaLocalDate()),
              textAlign = TextAlign.End,
            )
          },
        )
      }

      if (submitDate != null) {
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            Text(
              text = stringResource(R.string.claim_status_claim_details_submitted),
            )
          },
          endSlot = {
            Text(
              text = dateTimeFormatter.format(submitDate.toJavaLocalDate()),
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
private fun PreviewClaimDetailScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimDetailScreen(
        uiState = ClaimDetailUiState.Content(
          claimId = "id",
          submittedContent = ClaimDetailUiState.Content.SubmittedContent.FreeText("Some free input text"),
          claimStatusCardUiState = ClaimStatusCardUiState(
            id = "id",
            pillTypes = listOf(
              ClaimPillType.Open,
              ClaimPillType.Reopened,
              ClaimPillType.Closed.Paid,
              ClaimPillType.PaymentAmount(UiMoney(399.0, CurrencyCode.SEK)),
              ClaimPillType.Closed.NotCompensated,
              ClaimPillType.Closed.NotCovered,
            ),
            claimProgressItemsUiState = listOf(
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.Submitted,
                ClaimProgressSegment.SegmentType.PAID,
              ),
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.BeingHandled,
                ClaimProgressSegment.SegmentType.PAID,
              ),
              ClaimProgressSegment(
                ClaimProgressSegment.SegmentText.Closed,
                ClaimProgressSegment.SegmentType.PAID,
              ),
            ),
            claimType = "Broken item",
            insuranceDisplayName = "Home Insurance Homeowner",
          ),
          claimStatus = ClaimDetailUiState.Content.ClaimStatus.CLOSED,
          claimOutcome = ClaimDetailUiState.Content.ClaimOutcome.PAID,
          files = listOf(
            UiFile(
              id = "1",
              name = "test",
              mimeType = "",
              url = "1",
              localPath = null,
              thumbnailUrl = "1",
            ),
            UiFile(
              id = "2",
              name = "test".repeat(10),
              mimeType = "",
              url = "1",
              thumbnailUrl = "1",
              localPath = null,
            ),
            UiFile(
              id = "3",
              name = "test",
              mimeType = "",
              url = "1",
              thumbnailUrl = "1",
              localPath = null,
            ),
            UiFile(
              id = "4",
              name = "test4",
              mimeType = "",
              url = "1",
              thumbnailUrl = "1",
              localPath = null,
            ),
            UiFile(
              id = "5",
              name = "test5",
              mimeType = "",
              url = "1",
              thumbnailUrl = "1",
              localPath = null,
            ),
            UiFile(
              id = "6",
              name = "test6",
              mimeType = "",
              url = "",
              thumbnailUrl = "1",
              localPath = null,
            ),
          ),
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
        onChatClick = {},
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
