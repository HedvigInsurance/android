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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.ui.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
internal fun AddFilesDestination(
  viewModel: AddFilesViewModel,
  navigateUp: () -> Unit,
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
) {
  var showFileTypeSelectBottomSheet by remember { mutableStateOf(false) }

  if (showFileTypeSelectBottomSheet) {
    FilePickerBottomSheet(
      onPickPhoto = {
        onPickPhoto()
        showFileTypeSelectBottomSheet = false
      },
      onPickFile = {
        onPickFile()
        showFileTypeSelectBottomSheet = false
      },
      onTakePhoto = {
        launchTakePhotoRequest()
        showFileTypeSelectBottomSheet = false
      },
      onDismiss = {
        showFileTypeSelectBottomSheet = false
      },
    )
  }

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
    )
  }

  Surface(
    color = MaterialTheme.colorScheme.background,
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
            onAddMoreFilesButtonClick = {
              showFileTypeSelectBottomSheet = true
            },
            onContinueButtonClick = onContinue,
            isLoading = uiState.isLoading,
          )
        },
        bottomSpacing = {
          BottomSpacing()
        },
        files = uiState.localFiles,
        onRemoveFile = { fileToRemoveId = it },
        imageLoader = imageLoader,
        gridContentPaddingValues = PaddingValues(),
        modifier = Modifier.padding(
          PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal,
          )
            .asPaddingValues(),
        ),
        onClickFile = null,
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
  Spacer(Modifier.height(16.dp))
  HedvigSecondaryContainedButton(
    text = stringResource(R.string.claim_status_detail_add_more_files),
    onClick = onAddMoreFilesButtonClick,
  )
  Spacer(Modifier.height(8.dp))
  HedvigContainedButton(
    text = stringResource(R.string.general_continue_button),
    onClick = onContinueButtonClick,
    isLoading = isLoading,
  )
}

@Composable
private fun BottomSpacing() {
  Spacer(Modifier.height(16.dp))
  Spacer(
    Modifier.windowInsetsPadding(
      WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal +
          WindowInsetsSides.Bottom,
      ),
    ),
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewAddFilesScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddFilesScreen(
        FileUploadUiState(
          localFiles = List(25) {
            UiFile(
              name = "$it",
              localPath = "",
              url = "",
              mimeType = "$it",
              thumbnailUrl = null,
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
      )
    }
  }
}
