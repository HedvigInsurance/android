package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep

@Composable
internal fun TerminationSurveyDestination(
  viewModel: TerminationSurveyViewModel,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateToNextStep: (
    step: TerminateInsuranceStep,
    terminatableInsurance: TerminatableInsurance,
  ) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminationSurveyScreen(
      uiState, navigateUp, openChat, closeTerminationFlow, navigateToNextStep,
  )
}

@Composable
private fun TerminationSurveyScreen(
  uiState: TerminationSurveyState,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateToNextStep: (
    step: TerminateInsuranceStep,
    terminatableInsurance: TerminatableInsurance,
  ) -> Unit,
) {
  TODO()
}
