package com.hedvig.android.feature.chip.id.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class AddChipIdViewModel : MoleculeViewModel<AddChipIdEvent, AddChipIdUiState>(
  initialState = AddChipIdUiState.Loading,
  presenter = AddChipIdPresenter(),
)

internal class AddChipIdPresenter : MoleculePresenter<AddChipIdEvent, AddChipIdUiState> {
  @Composable
  override fun MoleculePresenterScope<AddChipIdEvent>.present(
    lastState: AddChipIdUiState,
  ): AddChipIdUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      currentState = AddChipIdUiState.Loading
      // TODO: Load chip ID data here
      currentState = AddChipIdUiState.Content
    }

    CollectEvents { event ->
      when (event) {
        AddChipIdEvent.Reload -> loadIteration++
      }
    }

    return currentState
  }
}

internal sealed interface AddChipIdUiState {
  data object Loading : AddChipIdUiState
  data object Content : AddChipIdUiState
  data class Failure(val message: String) : AddChipIdUiState
}

internal sealed interface AddChipIdEvent {
  data object Reload : AddChipIdEvent
}
