package com.hedvig.android.feature.terminateinsurance.step.deflectAutoCancel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DeflectAutoCancelStepViewModel(
  val message: String
) : MoleculeViewModel<Unit, DeflectAutoCancelUiState>(
  initialState = DeflectAutoCancelUiState.Success(message),
  presenter = DeflectAutoCancelStepPresenter(),
)

private class DeflectAutoCancelStepPresenter(
) : MoleculePresenter<Unit, DeflectAutoCancelUiState> {
  @Composable
  override fun MoleculePresenterScope<Unit>.present(
    lastState: DeflectAutoCancelUiState,
  ): DeflectAutoCancelUiState {
    var currentState by remember {
      mutableStateOf(lastState)
    }

    return currentState
  }
}

internal sealed interface DeflectAutoCancelUiState {
  data class Success(val message: String) : DeflectAutoCancelUiState
}
