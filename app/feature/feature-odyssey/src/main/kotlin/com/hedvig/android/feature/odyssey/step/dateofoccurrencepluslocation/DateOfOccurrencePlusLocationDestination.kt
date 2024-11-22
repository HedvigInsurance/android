package com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.feature.odyssey.ui.DatePickerWithDialog
import com.hedvig.android.feature.odyssey.ui.LocationWithDialog
import hedvig.resources.R
import java.util.Locale

@Composable
internal fun DateOfOccurrencePlusLocationDestination(
  viewModel: DateOfOccurrencePlusLocationViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  DateOfOccurrencePlusLocationScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    selectLocationOption = viewModel::selectLocationOption,
    submitDateOfOccurrenceAndLocation = viewModel::submitDateOfOccurrenceAndLocation,
    showedError = viewModel::showedError,
    navigateUp = navigateBack,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun DateOfOccurrencePlusLocationScreen(
  uiState: DateOfOccurrencePlusLocationUiState,
  windowSizeClass: WindowSizeClass,
  selectLocationOption: (LocationOption) -> Unit,
  submitDateOfOccurrenceAndLocation: () -> Unit,
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
    Text(
      text = stringResource(R.string.CLAIMS_LOCATON_OCCURANCE_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    Location(uiState, selectLocationOption, sideSpacingModifier.fillMaxWidth())
    Spacer(Modifier.height(4.dp))
    DateOfIncident(uiState.datePickerUiState, !uiState.isLoading, sideSpacingModifier.fillMaxWidth())
    Spacer(Modifier.height(16.dp))
    VectorInfoCard(
      text = stringResource(R.string.CLAIMS_DATE_NOT_SURE_NOTICE_LABEL),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitDateOfOccurrenceAndLocation,
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun DateOfIncident(uiState: DatePickerUiState, canInteract: Boolean, modifier: Modifier = Modifier) {
  DatePickerWithDialog(
    uiState = uiState,
    canInteract = canInteract,
    startText = stringResource(R.string.claims_item_screen_date_of_incident_button),
    modifier = modifier,
  )
}

@Composable
private fun Location(
  uiState: DateOfOccurrencePlusLocationUiState,
  selectLocationOption: (LocationOption) -> Unit,
  modifier: Modifier = Modifier,
) {
  LocationWithDialog(
    locationOptions = uiState.locationOptions,
    selectedLocation = uiState.selectedLocation,
    selectLocationOption = selectLocationOption,
    enabled = !uiState.isLoading,
    modifier = modifier,
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewDateOfOccurrencePlusLocationScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DateOfOccurrencePlusLocationScreen(
        uiState = DateOfOccurrencePlusLocationUiState(
          datePickerUiState = remember { DatePickerUiState(Locale.ENGLISH, null) },
          locationOptions = emptyList(),
          selectedLocation = null,
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        selectLocationOption = {},
        submitDateOfOccurrenceAndLocation = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
      )
    }
  }
}
