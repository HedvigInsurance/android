package com.hedvig.android.feature.change.tier.ui.stepstart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Failure
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Loading
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState.Success
import hedvig.resources.R

@Composable
internal fun StartChangeTierFlowDestination(
  viewModel: StartTierFlowViewModel,
  popBackStack: () -> Unit,
  launchFlow: (InsuranceCustomizationParameters) -> Unit,
) {
  val uiState: StartTierChangeState by viewModel.uiState.collectAsStateWithLifecycle()
  when (uiState) {
    is Failure -> {
      FailureScreen(
        reload = {
          viewModel.emit(StartTierChangeEvent.Reload)
        },
        popBackStack = popBackStack,
        reason = (uiState as Failure).reason,
      )
    }

    Loading -> HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(R.string.TIER_FLOW_PROCESSING),
    )
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

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit, reason: FailureReason) {
  Box(Modifier.fillMaxSize()) {
    when (reason) {
      GENERAL -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.fillMaxSize(),
        )
      }
      QUOTES_ARE_EMPTY -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal +
                  WindowInsetsSides.Bottom,
              ),
            ),
        ) {
          Spacer(Modifier.weight(1f))
          EmptyState(
            text = stringResource(R.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
            iconStyle = INFO,
            buttonStyle = NoButton,
            description = null,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.weight(1f))
          HedvigTextButton(
            stringResource(R.string.general_close_button),
            onClick = popBackStack,
            buttonSize = Large,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(32.dp))
        }
      }
    }
  }
}
