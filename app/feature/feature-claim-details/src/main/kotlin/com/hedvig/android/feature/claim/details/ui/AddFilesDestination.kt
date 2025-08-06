package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.logger.logcat
import com.hedvig.android.shared.file.upload.ui.FilePickerBottomSheet
import hedvig.resources.R

@Composable
internal fun AddFilesDestination(
  viewModel: AddFilesViewModel,
  navigateUp: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState.uploadedFileIds) {
    if (uiState.uploadedFileIds.isNotEmpty()) {
      navigateUp()
    }
  }

  val addLocalFile = viewModel::addLocalFile
  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    logcat { "ChatFileState sending uri:$uri" }
    addLocalFile(uri)
  }
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }

  AddFilesScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
    onContinue = viewModel::uploadFiles,
    onRemove = viewModel::onRemoveFile,
    onDismissError = viewModel::dismissError,
    launchTakePhotoRequest = photoCaptureState::launchTakePhotoRequest,
    onPickPhoto = { photoPicker.launch(PickVisualMediaRequest()) },
    onPickFile = { filePicker.launch("*/*") },
    onNavigateToImageViewer = onNavigateToImageViewer,
  )
}

@Composable
private fun AddFilesScreen(
  uiState: FileUploadUiState,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onContinue: () -> Unit,
  onRemove: (String) -> Unit,
  onDismissError: () -> Unit,
  launchTakePhotoRequest: () -> Unit,
  onPickPhoto: () -> Unit,
  onPickFile: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
) {
  val fileTypeSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  FilePickerBottomSheet(
    sheetState = fileTypeSelectBottomSheetState,
    onPickPhoto = {
      onPickPhoto()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onPickFile = {
      onPickFile()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onTakePhoto = {
      launchTakePhotoRequest()
      fileTypeSelectBottomSheetState.dismiss()
    },
  )

  var fileToRemoveId by remember { mutableStateOf<String?>(null) }
  if (fileToRemoveId != null) {
    HedvigAlertDialog(
      title = stringResource(id = R.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(id = R.string.CLAIMS_FILE_UPLOAD_REMOVE_SUBTITLE),
      confirmButtonLabel = stringResource(id = R.string.REMOVE_CONFIRMATION_BUTTON),
      dismissButtonLabel = stringResource(id = R.string.general_cancel_button),
      onDismissRequest = {
        fileToRemoveId = null
      },
      onConfirmClick = {
        fileToRemoveId?.let(onRemove)
      },
    )
  }

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = onDismissError,
      title = stringResource(R.string.something_went_wrong),
    )
  }

  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        onClick = navigateUp,
        title = stringResource(R.string.CLAIMS_YOUR_CLAIM),
      )
      DynamicFilesGridBetweenOtherThings(
        belowGridContent = {
          BelowGridContent(
            onAddMoreFilesButtonClick = fileTypeSelectBottomSheetState::show,
            onContinueButtonClick = onContinue,
            isLoading = uiState.isLoading,
          )
        },
        files = uiState.localFiles,
        onRemoveFile = { fileToRemoveId = it },
        imageLoader = imageLoader,
        contentPadding =
          PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
          )
            .asPaddingValues(),
        onClickFile = null,
        onNavigateToImageViewer = onNavigateToImageViewer,
      )
    }
  }
}

@Composable
private fun BelowGridContent(
  onAddMoreFilesButtonClick: () -> Unit,
  onContinueButtonClick: () -> Unit,
  isLoading: Boolean,
) {
  Column {
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.claim_status_detail_add_more_files),
      onClick = onAddMoreFilesButtonClick,
      modifier = Modifier.fillMaxWidth(),
      buttonStyle = Secondary,
      enabled = true,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onContinueButtonClick,
      modifier = Modifier.fillMaxWidth(),
      isLoading = isLoading,
      enabled = true,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewAddFilesScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddFilesScreen(
        FileUploadUiState(
          localFiles = List(25) {
            UiFile(
              name = "$it",
              localPath = "",
              url = "",
              mimeType = "$it",
              id = "$it",
            )
          },
        ),
        {},
        rememberPreviewImageLoader(),
        {},
        {},
        {},
        {},
        {},
        {},
        { _, _ -> },
      )
    }
  }
}
