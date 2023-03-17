package com.hedvig.android.odyssey.step.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.odyssey.data.ClaimFlowStep

@Composable
internal fun ClaimFlowStartDestination(
  viewModel: ClaimFlowStartStepViewModel,
  retryLoad: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
) {
  val uiState: ClaimFlowUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val terminationStep = (uiState as? ClaimFlowUiState.Success)?.nextStep
  LaunchedEffect(terminationStep) {
    if (terminationStep != null) {
      navigateToNextStep(terminationStep)
    }
  }
  TerminationStartScreen(uiState, retryLoad)
}

@Composable
private fun TerminationStartScreen(
  uiState: ClaimFlowUiState,
  retryLoad: () -> Unit,
) {
  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimFlowUiState.Error -> {
        GenericErrorScreen(
          onRetryButtonClick = retryLoad,
          modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),
        )
      }
      ClaimFlowUiState.Loading,
      is ClaimFlowUiState.Success,
      -> FullScreenHedvigProgress()
    }
  }
}
