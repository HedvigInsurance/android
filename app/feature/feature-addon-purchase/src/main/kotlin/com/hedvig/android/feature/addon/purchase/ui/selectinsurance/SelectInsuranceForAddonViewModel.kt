package com.hedvig.android.feature.addon.purchase.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.addon.purchase.data.GetInsuranceForTravelAddonUseCase
import com.hedvig.android.feature.addon.purchase.data.InsuranceForAddon
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectInsuranceForAddonViewModel(
  ids: List<String>,
  getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
) : MoleculeViewModel<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState>(
  initialState = SelectInsuranceForAddonState.Loading,
  presenter = SelectInsuranceForAddonPresenter(
    ids = ids,
    getInsuranceForTravelAddonUseCase = getInsuranceForTravelAddonUseCase,
  ),
)

internal class SelectInsuranceForAddonPresenter(
  private val ids: List<String>,
  private val getInsuranceForTravelAddonUseCase: GetInsuranceForTravelAddonUseCase,
) : MoleculePresenter<SelectInsuranceForAddonEvent, SelectInsuranceForAddonState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForAddonEvent>.present(
    lastState: SelectInsuranceForAddonState,
  ): SelectInsuranceForAddonState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    CollectEvents { event ->
      when (event) {
        SelectInsuranceForAddonEvent.Reload -> {
          loadIteration++
        }

        is SelectInsuranceForAddonEvent.SelectInsurance -> TODO()
        is SelectInsuranceForAddonEvent.SubmitSelected -> TODO()
      }
    }

    LaunchedEffect(loadIteration) {
      if (ids.isEmpty()) {
        //should be impossible btw
        currentState = SelectInsuranceForAddonState.Failure
      } else if (ids.size == 1) {
        //should be impossible: we reroute earlier in the navGraph
        currentState = SelectInsuranceForAddonState.Success(
          emptyList(),
          null,
          ids[0],
        )
      } else {
        getInsuranceForTravelAddonUseCase.invoke(ids).collect { result ->
          result.fold(
            ifLeft = {
              currentState = SelectInsuranceForAddonState.Failure
            },
            ifRight = { loadedInsurances ->
              currentState = SelectInsuranceForAddonState.Success(
                listOfInsurances = loadedInsurances,
                insuranceIdToContinue = null,
                currentlySelected = null,
              )
            },
          )
        }
      }
    }
    return currentState
  }
}

internal sealed interface SelectInsuranceForAddonState {
  data object Loading : SelectInsuranceForAddonState

  data class Success(
    val listOfInsurances: List<InsuranceForAddon>,
    val currentlySelected: InsuranceForAddon?,
    val insuranceIdToContinue: String? = null,
  ) : SelectInsuranceForAddonState

  data object Failure : SelectInsuranceForAddonState
}

internal sealed interface SelectInsuranceForAddonEvent {
  data object Reload : SelectInsuranceForAddonEvent
  data class SelectInsurance(val selected: InsuranceForAddon) : SelectInsuranceForAddonEvent
  data class SubmitSelected(val selected: InsuranceForAddon) : SelectInsuranceForAddonEvent
}
