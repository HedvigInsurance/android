package com.hedvig.feature.remove.addons.ui

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
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.InsuranceForAddon

internal class SelectInsuranceToRemoveAddonViewModel(
  getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
) : MoleculeViewModel<
    SelectInsuranceToRemoveAddonEvent,
    SelectInsuranceToRemoveAddonState,
  >(
    initialState = SelectInsuranceToRemoveAddonState.Loading,
    presenter = SelectInsuranceToRemoveAddonPresenter(getInsurancesWithRemovableAddonsUseCase),
  )

private class SelectInsuranceToRemoveAddonPresenter(
  val getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
) : MoleculePresenter<
    SelectInsuranceToRemoveAddonEvent,
    SelectInsuranceToRemoveAddonState,
  > {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceToRemoveAddonEvent>.present(
    lastState: SelectInsuranceToRemoveAddonState,
  ): SelectInsuranceToRemoveAddonState {
    var currentState: SelectInsuranceToRemoveAddonState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var insuranceIdToContinue by remember { mutableStateOf<String?>(null) }
    var currentlySelected by remember { mutableStateOf<InsuranceForAddon?>(null) }

    LaunchedEffect(loadIteration) {
      getInsurancesWithRemovableAddonsUseCase.invoke().fold(
        ifLeft = {
          currentState = SelectInsuranceToRemoveAddonState.Error
        },
        ifRight = {
          if (it.isEmpty()) {
            currentState = SelectInsuranceToRemoveAddonState.EmptyList
          } else {
            currentState = SelectInsuranceToRemoveAddonState.Success(
              listOfInsurances = it,
              currentlySelected = null,
              insuranceIdToContinue = if (it.size == 1) it.first().id else null,
            )
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SelectInsuranceToRemoveAddonEvent.Reload -> {
          loadIteration++
        }

        SelectInsuranceToRemoveAddonEvent.ClearNavigation -> {
          insuranceIdToContinue = null
        }

        is SelectInsuranceToRemoveAddonEvent.SelectInsurance -> {
          val state = currentState as? SelectInsuranceToRemoveAddonState.Success ?: return@CollectEvents
          val selected = state.listOfInsurances.firstOrNull { it.id == event.contractId }
          if (selected == null) return@CollectEvents
          currentlySelected = selected
        }

        is SelectInsuranceToRemoveAddonEvent.SubmitSelected -> {
          insuranceIdToContinue = currentlySelected?.id
        }
      }
    }
    return when (val state = currentState) {
      SelectInsuranceToRemoveAddonState.Error,
      SelectInsuranceToRemoveAddonState.Loading,
      SelectInsuranceToRemoveAddonState.EmptyList,
      -> state

      is SelectInsuranceToRemoveAddonState.Success -> state.copy(
        insuranceIdToContinue = insuranceIdToContinue,
        currentlySelected = currentlySelected,
      )
    }
  }
}

internal sealed interface SelectInsuranceToRemoveAddonState {
  data class Success(
    val listOfInsurances: List<InsuranceForAddon>,
    val currentlySelected: InsuranceForAddon?,
    val insuranceIdToContinue: String? = null,
  ) : SelectInsuranceToRemoveAddonState

  data object Error : SelectInsuranceToRemoveAddonState

  data object Loading : SelectInsuranceToRemoveAddonState

  data object EmptyList : SelectInsuranceToRemoveAddonState
}

internal sealed interface SelectInsuranceToRemoveAddonEvent {
  data object Reload : SelectInsuranceToRemoveAddonEvent

  data object ClearNavigation : SelectInsuranceToRemoveAddonEvent

  data class SelectInsurance(val contractId: String) : SelectInsuranceToRemoveAddonEvent

  data class SubmitSelected(val contractId: String) : SelectInsuranceToRemoveAddonEvent
}
