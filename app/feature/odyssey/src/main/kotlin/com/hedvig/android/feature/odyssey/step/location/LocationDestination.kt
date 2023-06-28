package com.hedvig.android.feature.odyssey.step.location

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.feature.odyssey.data.ClaimFlowStep
import com.hedvig.android.feature.odyssey.navigation.LocationOption
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import com.hedvig.android.feature.odyssey.ui.LocationWithDialog
import hedvig.resources.R

@Composable
internal fun LocationDestination(
  viewModel: LocationViewModel,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
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
    imageLoader = imageLoader,
    selectLocation = viewModel::selectLocationOption,
    submitLocation = viewModel::submitLocation,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
  )
}

@Composable
private fun LocationScreen(
  uiState: LocationUiState,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  selectLocation: (LocationOption) -> Unit,
  submitLocation: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    isLoading = uiState.isLoading,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.error,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.claims_incident_screen_location),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    LocationWithDialog(
      locationOptions = uiState.locationOptions,
      selectedLocation = uiState.selectedLocation,
      selectLocationOption = selectLocation,
      enabled = !uiState.isLoading,
      imageLoader = imageLoader,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitLocation,
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
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      LocationScreen(
        uiState = LocationUiState(
          locationOptions = List(3) {
            LocationOption("#$it", "Location #$it")
          },
          selectedLocation = LocationOption("#1", "Location #1"),
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        imageLoader = rememberPreviewImageLoader(),
        selectLocation = {},
        submitLocation = {},
        showedError = {},
        navigateUp = {},
      )
    }
  }
}
