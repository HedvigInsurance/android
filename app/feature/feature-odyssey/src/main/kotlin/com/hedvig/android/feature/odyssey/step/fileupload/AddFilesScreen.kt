package com.hedvig.android.feature.odyssey.step.fileupload

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DynamicFilesGridBetweenOtherThings
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun AddFilesScreen(
  uiState: FileUploadUiState,
  windowSizeClass: WindowSizeClass,
  onContinue: () -> Unit,
  onAddMoreFiles: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
  onRemoveFile: (fileId: String) -> Unit,
  imageLoader: ImageLoader,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    topAppBarText = stringResource(R.string.claim_status_detail_uploaded_files_info_title),
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.errorMessage != null,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    DynamicFilesGridBetweenOtherThings(
      files = (uiState.uploadedFiles + uiState.localFiles),
      imageLoader = imageLoader,
      onRemoveFile = onRemoveFile,
      onClickFile = null,
      belowGridContent = {
        BelowContent(
          isLoading = uiState.isLoading,
          onAddMoreFilesClick = onAddMoreFiles,
          onContinueClick = onContinue,
        )
      },
      modifier = sideSpacingModifier,
      contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
    )
  }
}

@Composable
private fun BelowContent(
  modifier: Modifier = Modifier,
  isLoading: Boolean,
  onAddMoreFilesClick: () -> Unit,
  onContinueClick: () -> Unit,
) {
  Column {
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.claim_status_detail_add_more_files),
      enabled = true,
      buttonStyle = Secondary,
      onClick = onAddMoreFilesClick,
      modifier = modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onContinueClick,
      isLoading = isLoading,
      enabled = !isLoading,
      modifier = modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigMultiScreenPreview
@Composable
private fun AddFilesScreenPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AddFilesScreen(
        uiState = FileUploadUiState(
          localFiles = List(25) {
            UiFile(
              "$it",
              "",
              "",
              "$it",
              "$it",
            )
          },
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        onContinue = {},
        onAddMoreFiles = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
        onRemoveFile = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}
