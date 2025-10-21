package com.hedvig.android.feature.terminateinsurance.step.deflectAutoDecom

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DeflectAutoDecomStepViewModel : MoleculeViewModel<DeflectAutoDecomEvent, DeflectAutoDecomUiState>(
  initialState = DeflectAutoDecomUiState.Loading,
  presenter = DeflectAutoDecomStepPresenter(),
)

private class DeflectAutoDecomStepPresenter() : MoleculePresenter<DeflectAutoDecomEvent, DeflectAutoDecomUiState> {
  @Composable
  override fun MoleculePresenterScope<DeflectAutoDecomEvent>.present(
    lastState: DeflectAutoDecomUiState,
  ): DeflectAutoDecomUiState {
    TODO("Not yet implemented")
  }
}

internal sealed interface DeflectAutoDecomUiState {
  data object Loading : DeflectAutoDecomUiState
}

internal sealed interface DeflectAutoDecomEvent
