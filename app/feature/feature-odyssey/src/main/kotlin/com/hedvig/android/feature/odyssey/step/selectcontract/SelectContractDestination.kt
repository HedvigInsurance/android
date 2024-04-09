package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
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
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocalContractContractOption
import com.hedvig.android.feature.odyssey.ui.ContractOptionWithDialog
import hedvig.resources.R

@Composable
internal fun SharedTransitionScope.SelectContractDestination(
  animatedContentScope: AnimatedContentScope,
  viewModel: SelectContractViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  SelectContractScreen(
    animatedContentScope = animatedContentScope,
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocation = viewModel::selectContractOption,
    submitLocation = viewModel::submitContract,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun SharedTransitionScope.SelectContractScreen(
  animatedContentScope: AnimatedContentScope,
  uiState: SelectContractUiState,
  windowSizeClass: WindowSizeClass,
  selectLocation: (LocalContractContractOption) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    animatedContentScope = animatedContentScope,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.error,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.CLAIM_TRIAGING_ABOUT_TITILE),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    ContractOptionWithDialog(
      locationOptions = uiState.contractOptions,
      selectedLocation = uiState.selectedContract,
      selectLocationOption = selectLocation,
      enabled = !uiState.isLoading,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitLocation,
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewLocationScreen() {
  HedvigPreviewLayout { animatedContentScope ->
    SelectContractScreen(
      animatedContentScope = animatedContentScope,
      uiState = SelectContractUiState(
        contractOptions = List(3) {
          LocalContractContractOption("#$it", "Location #$it")
        },
        selectedContract = LocalContractContractOption("#1", "Location #1"),
      ),
      windowSizeClass = WindowSizeClass.calculateForPreview(),
      selectLocation = {},
      submitLocation = {},
      showedError = {},
      navigateUp = {},
      closeClaimFlow = {},
    )
  }
}
