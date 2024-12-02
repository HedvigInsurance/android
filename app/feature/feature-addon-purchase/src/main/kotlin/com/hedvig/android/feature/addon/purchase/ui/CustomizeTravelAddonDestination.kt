package com.hedvig.android.feature.addon.purchase.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.feature.addon.purchase.data.TravelAddonOption

@Composable
internal fun CustomizeTravelAddonDestination(
  viewModel: CustomizeTravelAddonViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  navigateToSummary: (travelAddonOption: TravelAddonOption) -> Unit,
) {
  val uiState: CustomizeTravelAddonState by viewModel.uiState.collectAsStateWithLifecycle()
  CustomizeTravelAddonScreen(uiState)
}


@Composable
private fun CustomizeTravelAddonScreen(
  uiState: CustomizeTravelAddonState
) {
  Box(
    Modifier.fillMaxSize(),
  ) {
    when (val state = uiState) {
      CustomizeTravelAddonState.Failure -> TODO()
      CustomizeTravelAddonState.Loading -> TODO()
      is CustomizeTravelAddonState.Success -> TODO()
    }
  }
}
