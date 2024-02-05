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
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FilesGridScreen
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold

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
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.errorMessage != null,
      showedError = showedError,
    ),
    scrollable = false,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    FilesGridScreen(
      files = (uiState.uploadedFiles + uiState.localFiles),
      onContinue = onContinue,
      onAddMoreFiles = onAddMoreFiles,
      onRemoveFile = onRemoveFile,
      imageLoader = imageLoader,
      isLoading = uiState.isLoading,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun AddFilesScreenPreview() {
  HedvigTheme {
    Surface {
      AddFilesScreen(
        uiState = FileUploadUiState(
          localFiles = listOf(
            UiFile(
              "123",
              "",
              "",
              "123123123123123123123",
            ),
            UiFile(
              "123",
              "",
              "",
              "1234",
            ),
            UiFile(
              "123",
              "",
              "",
              "1232",
            ),
            UiFile(
              "123",
              "",
              "",
              "1123",
            ),
          ),
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
