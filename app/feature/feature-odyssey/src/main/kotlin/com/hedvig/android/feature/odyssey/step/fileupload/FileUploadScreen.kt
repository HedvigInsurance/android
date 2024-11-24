package com.hedvig.android.feature.odyssey.step.fileupload

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
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
    HedvigText(
      text = stringResource(R.string.CLAIMS_FILE_UPLOAD_TITLE),
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
      ),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    HedvigNotificationCard(
      message = stringResource(id = R.string.CLAIMS_FILE_UPLOAD_INFO),
      priority = Info,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.file_upload_upload_files),
      onClick = submitFiles,
      isLoading = uiState.isLoading,
      enabled = !uiState.isLoading,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.claims_skip_button),
      onClick = onContinue,
      enabled = !uiState.isLoading,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewFileUploadScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
