package com.hedvig.android.feature.terminateinsurance.step.deflectAutoCancel

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DeflectAutoCancelStepViewModel : MoleculeViewModel<DeflectAutoCancelEvent, DeflectAutoCancelUiState>(
  initialState = DeflectAutoCancelUiState.Loading,
  presenter = DeflectAutoCancelStepPresenter(),
)

private class DeflectAutoCancelStepPresenter() : MoleculePresenter<DeflectAutoCancelEvent, DeflectAutoCancelUiState> {
  @Composable
  override fun MoleculePresenterScope<DeflectAutoCancelEvent>.present(
    lastState: DeflectAutoCancelUiState,
  ): DeflectAutoCancelUiState {
    TODO("Not yet implemented")
  }
}

internal sealed interface DeflectAutoCancelUiState {
  data object Loading : DeflectAutoCancelUiState
}

internal sealed interface DeflectAutoCancelEvent
