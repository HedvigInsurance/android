package com.hedvig.android.feature.odyssey.step.location

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.LocationWithDialog
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun LocationDestination(
  viewModel: LocationViewModel,
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
  LocationScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocation = viewModel::selectLocationOption,
    submitLocation = viewModel::submitLocation,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun LocationScreen(
  uiState: LocationUiState,
  windowSizeClass: WindowSizeClass,
  selectLocation: (LocationOption) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.error,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.claims_incident_screen_location),
      style = HedvigTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    LocationWithDialog(
      locationOptions = uiState.locationOptions,
      selectedLocation = uiState.selectedLocation,
      selectLocationOption = selectLocation,
      enabled = !uiState.isLoading,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitLocation,
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewLocationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      LocationScreen(
        uiState = LocationUiState(
          locationOptions = List(3) {
            LocationOption("#$it", "Location #$it")
          },
          selectedLocation = LocationOption("#1", "Location #1"),
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
}
