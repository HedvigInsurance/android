package com.hedvig.android.feature.odyssey.step.fileupload

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import hedvig.resources.R

@Composable
internal fun FileUploadScreen(
  uiState: FileUploadUiState,
  windowSizeClass: WindowSizeClass,
  submitFiles: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  onContinue: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.errorMessage != null,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.CLAIMS_FILE_UPLOAD_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    VectorInfoCard(
      text = stringResource(id = R.string.CLAIMS_FILE_UPLOAD_INFO),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.file_upload_upload_files),
      onClick = submitFiles,
      isLoading = uiState.isLoading,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.claims_skip_button),
      onClick = onContinue,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewFileUploadScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FileUploadScreen(
        FileUploadUiState(),
        WindowSizeClass.calculateForPreview(),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
