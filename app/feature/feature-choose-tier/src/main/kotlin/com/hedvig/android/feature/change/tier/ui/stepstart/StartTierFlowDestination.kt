package com.hedvig.android.feature.change.tier.ui.stepstart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Failure
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Loading
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Success

@Composable
internal fun StartChangeTierFlowDestination(
  viewModel: StartTierFlowViewModel,
  navigateUp: () -> Unit,
  launchFlow: (InsuranceCustomizationParameters) -> Unit,
) {
  val uiState: StartTierChangeState by viewModel.uiState.collectAsStateWithLifecycle()
  when (uiState) {
    Failure ->
      HedvigScaffold(navigateUp) {
        HedvigErrorSection(
          onButtonClick = {
            viewModel.emit(StartTierChangeEvent.Reload)
          },
        )
      }

    Loading -> HedvigFullScreenCenterAlignedProgress()
    is Success -> {
      LaunchedEffect((uiState as Success).paramsToNavigate) {
        val params = (uiState as Success).paramsToNavigate
        if (params != null) {
          launchFlow(params)
        }
      }
    }
  }
}
