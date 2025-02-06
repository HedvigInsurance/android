package com.hedvig.android.feature.addon.purchase.ui.triage

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageState.Failure
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageState.Loading
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageState.Success
import hedvig.resources.R

@Composable
internal fun TravelAddonTriageDestination(
  viewModel: TravelAddonTriageViewModel,
  popBackStack: () -> Unit,
  launchFlow: (insuranceIds: List<String>) -> Unit,
) {
  val uiState: TravelAddonTriageState by viewModel.uiState.collectAsStateWithLifecycle()
  StartChangeTierFlowScreen(
    uiState = uiState,
    popBackStack = popBackStack,
    reload = {
      viewModel.emit(TravelAddonTriageEvent.Reload)
    },
    launchFlow = launchFlow,
  )
}

@Composable
private fun StartChangeTierFlowScreen(
  uiState: TravelAddonTriageState,
  popBackStack: () -> Unit,
  reload: () -> Unit,
  launchFlow: (List<String>) -> Unit,
) {
  when (uiState) {
    is Failure -> {
      FailureScreen(
        reload = reload,
        popBackStack = popBackStack,
        reason = uiState.reason,
      )
    }

    Loading -> HedvigFullScreenCenterAlignedProgress()

    is Success -> {
      LaunchedEffect(uiState.insuranceIds) {
        val params = uiState.insuranceIds
        launchFlow(params)
      }
    }
  }
}

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit, reason: FailureReason) {
  Box(Modifier.fillMaxSize()) {
    when (reason) {
      FailureReason.GENERAL -> {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.fillMaxSize(),
        )
      }

      FailureReason.NO_TRAVEL_ADDON_AVAILABLE -> {
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
            // todo: another string key here!
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

@HedvigMultiScreenPreview
@Composable
private fun StartTierFlowScreenPreview(
  @PreviewParameter(TravelAddonTriageStateProvider::class) state: TravelAddonTriageState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      StartChangeTierFlowScreen(
        uiState = state,
        {},
        {},
        {},
      )
    }
  }
}

internal class TravelAddonTriageStateProvider :
  CollectionPreviewParameterProvider<TravelAddonTriageState>(
    listOf(
      TravelAddonTriageState.Loading,
      Success(
        listOf("id", "id2"),
      ),
      Failure(FailureReason.GENERAL),
      Failure(FailureReason.NO_TRAVEL_ADDON_AVAILABLE),
    ),
  )
