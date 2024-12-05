package com.hedvig.android.feature.addon.purchase.ui.selectinsurance

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectInsuranceForAddonViewModel(ids: List<String>) : MoleculeViewModel<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState>(
  initialState = SelectInsuranceForAddonState.Loading,
  presenter = SelectInsuranceForAddonPresenter(ids = ids),
)

internal class SelectInsuranceForAddonPresenter(private val ids: List<String>) : MoleculePresenter<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForAddonEvent>.present(
    lastState: SelectInsuranceForAddonState,
  ): SelectInsuranceForAddonState {
    CollectEvents { event ->
      when (event) {
        SelectInsuranceForAddonEvent.Reload -> TODO()
      }
    }

    TODO("Not yet implemented")
  }
}

internal sealed interface SelectInsuranceForAddonState {
  data object Loading : SelectInsuranceForAddonState

  data object Success : SelectInsuranceForAddonState

  data object Failure : SelectInsuranceForAddonState
}

internal sealed interface SelectInsuranceForAddonEvent {
  data object Reload : SelectInsuranceForAddonEvent
}
