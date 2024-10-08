package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import hedvig.resources.R

@Composable
internal fun ChangeTierSummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  onSuccess: () -> Unit,
  onFailure: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState: SummaryState by viewModel.uiState.collectAsStateWithLifecycle()
  when (uiState) {
    Failure -> HedvigScaffold(navigateUp) {
      HedvigErrorSection(
        onButtonClick = {
          viewModel.emit(SummaryEvent.Reload)
        },
      )
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()

    MakingChanges -> MakingChangesScreen()

    is Success -> {
      LaunchedEffect((uiState as Success).navigateToFail) {
        val fail = (uiState as Success).navigateToFail
        if (fail) {
          viewModel.emit(SummaryEvent.ClearNavigation)
          onFailure()
        }
      }
      LaunchedEffect((uiState as Success).navigateToSuccess) {
        val success = (uiState as Success).navigateToSuccess
        if (success) {
          viewModel.emit(SummaryEvent.ClearNavigation)
          onSuccess()
        }
      }
      SummarySuccessScreen()
    }
  }
}

@Composable
private fun MakingChangesScreen() {
  HedvigFullScreenCenterAlignedLinearProgress(
      title =
      stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_LOADING_TITLE),
  )
}


@Composable
private fun SummarySuccessScreen() {

}
