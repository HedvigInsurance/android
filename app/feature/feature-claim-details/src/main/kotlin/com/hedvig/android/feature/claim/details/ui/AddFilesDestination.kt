package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.ui.FilesGridScreen
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
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

  AddFilesScreen(
    uiState = uiState,
    appPackageId = appPackageId,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
    onUri = viewModel::addLocalFile,
    onContinue = viewModel::uploadFiles,
    onRemove = viewModel::onRemoveFile,
    onDismissError = viewModel::dismissError,
  )
}

@Composable
private fun AddFilesScreen(
  uiState: FileUploadUiState,
  appPackageId: String,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  onUri: (Uri) -> Unit,
  onContinue: () -> Unit,
  onRemove: (String) -> Unit,
  onDismissError: () -> Unit,
) {
  var showFileTypeSelectBottomSheet by remember { mutableStateOf(false) }

  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    logcat { "ChatFileState sending uri:$uri" }
    onUri(uri)
  }
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { resultingUri: Uri? ->
    if (resultingUri != null) {
      onUri(resultingUri)
    }
  }
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { resultingUri: Uri? ->
    if (resultingUri != null) {
      onUri(resultingUri)
    }
  }

  if (showFileTypeSelectBottomSheet) {
    FilePickerBottomSheet(
      onPickPhoto = {
        photoPicker.launch(PickVisualMediaRequest())
        showFileTypeSelectBottomSheet = false
      },
      onPickFile = {
        filePicker.launch("*/*")
        showFileTypeSelectBottomSheet = false
      },
      onTakePhoto = {
        photoCaptureState.launchTakePhotoRequest()
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
      Spacer(Modifier.height(16.dp))
      FilesGridScreen(
        files = uiState.localFiles,
        onContinue = onContinue,
        onAddMoreFiles = {
          showFileTypeSelectBottomSheet = true
        },
        onRemoveFile = {
          fileToRemoveId = it
        },
        imageLoader = imageLoader,
        isLoading = uiState.isLoading,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}
