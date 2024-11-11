package com.hedvig.android.feature.odyssey.step.fileupload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.core.fileupload.ui.FilePickerBottomSheet
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.data.claimflow.ClaimFlowStep
import hedvig.resources.R

@Composable
internal fun FileUploadDestination(
  viewModel: FileUploadViewModel,
  navigateUp: () -> Unit,
  windowSizeClass: WindowSizeClass,
  closeClaimFlow: () -> Unit,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  appPackageId: String,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }

  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    viewModel.addLocalFile(uri)
  }
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      viewModel.addLocalFile(resultingUri)
    }
  }
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents(),
  ) { resultingUriList: List<Uri> ->
    for (resultingUri in resultingUriList) {
      viewModel.addLocalFile(resultingUri)
    }
  }

  var showFileTypeSelectBottomSheet by remember { mutableStateOf(false) }

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
    isVisible = showFileTypeSelectBottomSheet,
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
        fileToRemoveId?.let(viewModel::onRemoveFile)
      },
    )
  }

  if (uiState.hasFiles) {
    AddFilesScreen(
      uiState = uiState,
      windowSizeClass = windowSizeClass,
      onContinue = viewModel::onContinue,
      onAddMoreFiles = {
        showFileTypeSelectBottomSheet = true
      },
      showedError = viewModel::dismissError,
      navigateUp = navigateUp,
      closeClaimFlow = closeClaimFlow,
      imageLoader = imageLoader,
      onRemoveFile = {
        fileToRemoveId = it
      },
    )
  } else {
    FileUploadScreen(
      uiState = uiState,
      windowSizeClass = windowSizeClass,
      submitFiles = {
        showFileTypeSelectBottomSheet = true
      },
      onContinue = viewModel::onContinue,
      showedError = viewModel::dismissError,
      navigateUp = navigateUp,
      closeClaimFlow = closeClaimFlow,
    )
  }
}
