package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectAddonToRemoveViewModel() : MoleculeViewModel<
  SelectAddonToRemoveEvent, SelectAddonToRemoveState,
  >(
  initialState = SelectAddonToRemoveState.Loading,
  presenter = SelectAddonToRemovePresenter(),
)

private class SelectAddonToRemovePresenter : MoleculePresenter<
  SelectAddonToRemoveEvent, SelectAddonToRemoveState,
  > {
  @Composable
  override fun MoleculePresenterScope<SelectAddonToRemoveEvent>.present(
    lastState: SelectAddonToRemoveState,
  ): SelectAddonToRemoveState {
    TODO("Not yet implemented")
  }
}

internal sealed interface SelectAddonToRemoveState {
  data object Success : SelectAddonToRemoveState

  data object Error : SelectAddonToRemoveState

  data object Loading : SelectAddonToRemoveState
}

internal interface SelectAddonToRemoveEvent {
  data object Retry : SelectAddonToRemoveEvent
}
