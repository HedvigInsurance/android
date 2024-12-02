package com.hedvig.android.feature.addon.purchase.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.feature.addon.purchase.data.TravelAddonOption
import hedvig.resources.R

@Composable
internal fun CustomizeTravelAddonDestination(
  viewModel: CustomizeTravelAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
) {
  val uiState: CustomizeTravelAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    popBackStack = popBackStack,
    navigateToSummary = navigateToSummary,
    reload = {
      viewModel.emit(CustomizeTravelAddonEvent.Reload)
    }
  )
}


@Composable
private fun CustomizeTravelAddonScreen(
  uiState: CustomizeTravelAddonState,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
  reload: () -> Unit,
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      CustomizeTravelAddonState.Failure -> FailureScreen(reload, popBackStack)
      CustomizeTravelAddonState.Loading -> HedvigFullScreenCenterAlignedProgress()
      is CustomizeTravelAddonState.Success -> TODO()
    }
  }
}

@Composable
private fun FailureScreen(reload: () -> Unit, popBackStack: () -> Unit) {
  Box(Modifier.fillMaxSize()) {
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
      HedvigErrorSection(
        onButtonClick = reload,
        modifier = Modifier.fillMaxSize(),
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
