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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.core.icons.hedvig.normal.ArrowBack
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.ui.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
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
import octopus.type.CurrencyCode

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
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      ClaimDetailTopAppBar(
        navigateUp = navigateUp,
        navigateToConversation = uiState.safeCast<ClaimDetailUiState.Content>()?.let {
          { navigateToConversation(it.conversationId) }
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
private fun ClaimDetailTopAppBar(navigateUp: () -> Unit, navigateToConversation: (() -> Unit)?) {
  TopAppBar(
    title = {
      Text(
        text = stringResource(R.string.CLAIMS_YOUR_CLAIM),
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    windowInsets = TopAppBarDefaults.windowInsets,
    navigationIcon = {
      IconButton(
        onClick = navigateUp,
        content = {
          Icon(
            imageVector = Icons.Hedvig.ArrowBack,
            contentDescription = null,
          )
        },
      )
    },
    actions = {
      if (navigateToConversation != null) {
        IconButton(navigateToConversation, Modifier.size(40.dp)) {
          Icon(
            imageVector = Icons.Hedvig.Chat,
            contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
            tint = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.fillSecondary,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape),
          )
        }
        // TopAppBar has a default 4.dp padding horizontally
        // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/AppBar.kt;l=2890?q=private%20val%20TopAppBarHorizontalPadding%20%3D%204.dp&sq=&ss=androidx%2Fplatform%2Fframeworks%2Fsupport
        Spacer(Modifier.width((16 - 4).dp))
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
  )
}

@Composable
private fun BeforeGridContent(uiState: ClaimDetailUiState.Content, downloadFromUrl: (url: String) -> Unit) {
  Column {
    Spacer(Modifier.height(8.dp))
    HedvigCard {
      Column {
        ClaimStatusCardContent(uiState = uiState.claimStatusCardUiState)
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
            Text(
              text = stringResource(R.string.claim_status_title),
              style = com.hedvig.android.design.system.hedvig.HedvigTheme.typography.label,
            )
            Text(
              text = statusParagraphText(uiState.claimStatus, uiState.claimOutcome),
              style = com.hedvig.android.design.system.hedvig.HedvigTheme.typography.label.copy(
                color = HedvigTheme.colorScheme.textSecondary,
              ),
            )
          }
        }
      }
    }
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
    Spacer(Modifier.height(32.dp))
    if (uiState.uploadError != null) {
      ErrorDialog(
        message = uiState.uploadError,
        onDismiss = onDismissUploadError,
      )
    }
  }
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
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    com.hedvig.android.design.system.hedvig.HedvigTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
        ClaimDetailScreen(
          uiState = ClaimDetailUiState.Content(
            claimId = "id",
            conversationId = "idd",
            submittedContent = ClaimDetailUiState.Content.SubmittedContent.FreeText("Some free input text"),
            claimStatusCardUiState = ClaimStatusCardUiState(
              id = "id",
              claimType = "Broken item",
              insuranceDisplayName = null, // "Home Insurance Homeowner",
              submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
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
              ),
              UiFile(
                id = "2",
                name = "test".repeat(10),
                mimeType = "",
                url = "1",
                localPath = null,
              ),
              UiFile(
                id = "3",
                name = "test",
                mimeType = "",
                url = "1",
                localPath = null,
              ),
              UiFile(
                id = "4",
                name = "test4",
                mimeType = "",
                url = "1",
                localPath = null,
              ),
              UiFile(
                id = "5",
                name = "test5",
                mimeType = "",
                url = "1",
                localPath = null,
              ),
              UiFile(
                id = "6",
                name = "test6",
                mimeType = "",
                url = "",
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
}

@HedvigPreview
@Composable
private fun PreviewClaimDetailTopAppBar() {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    com.hedvig.android.design.system.hedvig.HedvigTheme {
      Surface(color = MaterialTheme.colorScheme.background) {
        ClaimDetailTopAppBar({}, {})
      }
    }
  }
}
