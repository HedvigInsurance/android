package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.feature.remove.addons.ChooseAddonToRemoveKey
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.InsuranceForAddon
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectInsuranceToRemoveAddonViewModel(
  getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
  backstack: Backstack,
) : MoleculeViewModel<
    SelectInsuranceToRemoveAddonEvent,
    SelectInsuranceToRemoveAddonState,
  >(
    initialState = SelectInsuranceToRemoveAddonState.Loading,
    presenter = SelectInsuranceToRemoveAddonPresenter(getInsurancesWithRemovableAddonsUseCase, backstack),
  )

private class SelectInsuranceToRemoveAddonPresenter(
  val getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
  private val backstack: Backstack,
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
    var currentlySelected by remember { mutableStateOf<InsuranceForAddon?>(null) }

    LaunchedEffect(loadIteration) {
      getInsurancesWithRemovableAddonsUseCase.invoke().fold(
        ifLeft = {
          currentState = SelectInsuranceToRemoveAddonState.Error
        },
        ifRight = {
          if (it.isEmpty()) {
            currentState = SelectInsuranceToRemoveAddonState.EmptyList
          } else if (it.size == 1) {
            // Single eligible insurance: skip the picker and jump straight to the addon picker.
            backstack.add(ChooseAddonToRemoveKey(it.first().contractId, preselectedAddonVariant = null))
          } else {
            currentState = SelectInsuranceToRemoveAddonState.Success(
              listOfInsurances = it,
              currentlySelected = null,
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

        is SelectInsuranceToRemoveAddonEvent.SelectInsurance -> {
          val state = currentState as? SelectInsuranceToRemoveAddonState.Success ?: return@CollectEvents
          val selected = state.listOfInsurances.firstOrNull { it.contractId == event.contractId }
          if (selected == null) return@CollectEvents
          currentlySelected = selected
        }

        is SelectInsuranceToRemoveAddonEvent.SubmitSelected -> {
          val selected = currentlySelected ?: return@CollectEvents
          backstack.add(ChooseAddonToRemoveKey(selected.contractId, preselectedAddonVariant = null))
        }
      }
    }
    return when (val state = currentState) {
      SelectInsuranceToRemoveAddonState.Error,
      SelectInsuranceToRemoveAddonState.Loading,
      SelectInsuranceToRemoveAddonState.EmptyList,
      -> state

      is SelectInsuranceToRemoveAddonState.Success -> state.copy(
        currentlySelected = currentlySelected,
      )
    }
  }
}

internal sealed interface SelectInsuranceToRemoveAddonState {
  data class Success(
    val listOfInsurances: List<InsuranceForAddon>,
    val currentlySelected: InsuranceForAddon?,
  ) : SelectInsuranceToRemoveAddonState

  data object Error : SelectInsuranceToRemoveAddonState

  data object Loading : SelectInsuranceToRemoveAddonState

  data object EmptyList : SelectInsuranceToRemoveAddonState
}

internal sealed interface SelectInsuranceToRemoveAddonEvent {
  data object Reload : SelectInsuranceToRemoveAddonEvent

  data class SelectInsurance(val contractId: ContractId) : SelectInsuranceToRemoveAddonEvent

  data class SubmitSelected(val contractId: ContractId) : SelectInsuranceToRemoveAddonEvent
}
