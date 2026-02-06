package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectInsuranceToRemoveAddonViewModel() : MoleculeViewModel<
  SelectInsuranceToRemoveAddonEvent, SelectInsuranceToRemoveAddonState>(
  initialState = SelectInsuranceToRemoveAddonState.Loading,
  presenter = SelectInsuranceToRemoveAddonPresenter())

private class SelectInsuranceToRemoveAddonPresenter: MoleculePresenter<
  SelectInsuranceToRemoveAddonEvent, SelectInsuranceToRemoveAddonState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceToRemoveAddonEvent>.present(
    lastState: SelectInsuranceToRemoveAddonState,
  ): SelectInsuranceToRemoveAddonState {
    TODO("Not yet implemented")
  }
}

internal sealed interface SelectInsuranceToRemoveAddonState {
  data object Success : SelectInsuranceToRemoveAddonState

  data object Error : SelectInsuranceToRemoveAddonState

  data object Loading : SelectInsuranceToRemoveAddonState
}

internal interface SelectInsuranceToRemoveAddonEvent {
  data object Retry : SelectInsuranceToRemoveAddonEvent
}
