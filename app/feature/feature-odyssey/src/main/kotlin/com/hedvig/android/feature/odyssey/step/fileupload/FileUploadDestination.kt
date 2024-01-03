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
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun FileUploadDestination(
  viewModel: FileUploadViewModel,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  windowSizeClass: WindowSizeClass,
  closeClaimFlow: () -> Unit,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }

  FileUploadScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    submitFiles = viewModel::onContinue,
    showedError = { },
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun FileUploadScreen(
  uiState: FileUploadUiState,
  windowSizeClass: WindowSizeClass,
  submitFiles: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
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
    VectorInfoCard(text = stringResource(id = R.string.CLAIMS_FILE_UPLOAD_INFO), modifier = sideSpacingModifier)
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitFiles,
      isLoading = uiState.isLoading,
      enabled = true,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}
