package com.hedvig.android.feature.addon.purchase.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var currentState by remember { mutableStateOf(lastState) }
    CollectEvents { event ->
      when (event) {
        SelectInsuranceForAddonEvent.Reload -> TODO()
      }
    }

    LaunchedEffect(Unit) {
      if (ids.isEmpty()) {
        //todo: should be impossible btw
        currentState = SelectInsuranceForAddonState.Failure
      } else if (ids.size==1) {
        //todo: should be impossible btw: we reroute in the navGraph
        //todo: redirect to CustomizeAddon
      } else {
        //todo: fetch contracts displayName
        //todo: Success/Failure
      }
    }
    return currentState
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
