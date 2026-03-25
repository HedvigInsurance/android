package com.hedvig.android.feature.chip.id.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText

@Composable
internal fun AddChipIdDestination(
  viewModel: AddChipIdViewModel,
  navigateUp: () -> Unit,
) {
  val uiState: AddChipIdUiState by viewModel.uiState.collectAsStateWithLifecycle()
  AddChipIdScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = {
      viewModel.emit(AddChipIdEvent.Reload)
    },
  )
}

@Composable
private fun AddChipIdScreen(
  uiState: AddChipIdUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
) {
  when (uiState) {
    AddChipIdUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    AddChipIdUiState.Content -> {
      HedvigScaffold(navigateUp = navigateUp) {
        Column(Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
          ) {
          HedvigText( "Chip id content")
        }
      }
    }

    is AddChipIdUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        // TODO: Add error UI here
      }
    }
  }
}
