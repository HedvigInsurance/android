package com.hedvig.android.feature.terminateinsurance.step.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedLoadingIndicatorDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep

@Composable
internal fun TerminationStartDestination(
  viewModel: TerminationStartStepViewModel,
  retryLoad: () -> Unit,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  val uiState: TerminationFlowUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val terminationStep = (uiState as? TerminationFlowUiState.Success)?.nextStep
  LaunchedEffect(terminationStep) {
    if (terminationStep != null) {
      navigateToNextStep(terminationStep)
    }
  }
  TerminationStartScreen(uiState, retryLoad)
}

@Composable
private fun TerminationStartScreen(uiState: TerminationFlowUiState, retryLoad: () -> Unit) {
  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      TerminationFlowUiState.Error -> HedvigErrorSection(retry = retryLoad)
      TerminationFlowUiState.Loading,
      is TerminationFlowUiState.Success,
      -> HedvigFullScreenCenterAlignedLoadingIndicatorDebounced()
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationStartScreenWithError() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationStartScreen(TerminationFlowUiState.Error) {}
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationStartScreenLoading() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationStartScreen(TerminationFlowUiState.Loading) {}
    }
  }
}
