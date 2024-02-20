package com.hedvig.android.feature.odyssey.step.fileupload

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FilesLazyVerticalGrid
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
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
    FilesLazyVerticalGrid(
      files = (uiState.uploadedFiles + uiState.localFiles),
      onRemoveFile = onRemoveFile,
      imageLoader = imageLoader,
      modifier = sideSpacingModifier
        .weight(1f)
        .layout { measurable, constraints ->
          // Add 8.dp to the min and max width to account for the internal padding coming from FilesLazyVerticalGrid
          // This is necessary as sideSpacingModifier does not really let us go outside of its bounds easily otherwise
          val placeable = measurable.measure(
            constraints.copy(
              minWidth = constraints.minWidth.plus(16.dp.roundToPx()),
              maxWidth = constraints.maxWidth.plus(16.dp.roundToPx()),
            ),
          )
          layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
          }
        },
    )
    Spacer(Modifier.height(8.dp))
    HedvigSecondaryContainedButton(
      text = stringResource(R.string.claim_status_detail_add_more_files),
      onClick = onAddMoreFiles,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = onContinue,
      isLoading = uiState.isLoading,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigMultiScreenPreview
@Composable
private fun AddFilesScreenPreview() {
  HedvigTheme {
    Surface {
      AddFilesScreen(
        uiState = FileUploadUiState(
          localFiles = List(25) {
            UiFile(
              "$it",
              "",
              "",
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
