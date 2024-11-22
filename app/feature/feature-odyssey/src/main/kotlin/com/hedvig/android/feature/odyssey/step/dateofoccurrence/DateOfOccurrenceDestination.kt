package com.hedvig.android.feature.odyssey.step.dateofoccurrence

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
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.feature.odyssey.ui.DatePickerWithDialog
import hedvig.resources.R
import java.util.Locale

@Composable
internal fun DateOfOccurrenceDestination(
  viewModel: DateOfOccurrenceViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  DateOfOccurrenceScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    submitSelectedDate = viewModel::submitSelectedDate,
    showedError = viewModel::showedError,
    navigateUp = navigateBack,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun DateOfOccurrenceScreen(
  uiState: DateOfOccurrenceUiState,
  windowSizeClass: WindowSizeClass,
  submitSelectedDate: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.dateSubmissionError,
      showedError = showedError,
    ),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.claims_incident_screen_date_of_incident),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    DatePickerWithDialog(
      uiState = uiState.datePickerUiState,
      canInteract = uiState.canSubmit,
      startText = stringResource(R.string.claims_item_screen_date_of_incident_button),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    VectorInfoCard(
      text = stringResource(R.string.CLAIMS_DATE_NOT_SURE_NOTICE_LABEL),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submitSelectedDate,
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
private fun PreviewDateOfOccurrenceScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DateOfOccurrenceScreen(
        uiState = DateOfOccurrenceUiState(
          datePickerUiState = DatePickerUiState(Locale.ENGLISH, null),
          dateSubmissionError = false,
          nextStep = null,
          isLoading = false,
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        submitSelectedDate = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
      )
    }
  }
}
