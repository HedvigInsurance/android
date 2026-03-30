package com.hedvig.android.feature.chip.id.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.chip.id.data.GetPetContractsForChipIdUseCase
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectInsuranceForChipIdViewModel(
  private val preselectedContractId: String?,
  private val getPetContractsForChipIdUseCase: GetPetContractsForChipIdUseCase,
) : MoleculeViewModel<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState>(
    initialState = SelectInsuranceForChipIdState.Loading,
    presenter = SelectInsuranceForChipIdPresenter(preselectedContractId, getPetContractsForChipIdUseCase),
  )

internal class SelectInsuranceForChipIdPresenter(
  private val preselectedContractId: String?,
  private val getPetContractsForChipIdUseCase: GetPetContractsForChipIdUseCase,
) : MoleculePresenter<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForChipIdEvent>.present(
    lastState: SelectInsuranceForChipIdState,
  ): SelectInsuranceForChipIdState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    var selectedContract: PetContractForChipId? by remember { mutableStateOf(null) }
    var contractIdToContinue: String? by remember { mutableStateOf(null) }

    LaunchedEffect(loadIteration) {
      currentState = SelectInsuranceForChipIdState.Loading
      val result = getPetContractsForChipIdUseCase.invoke()
      currentState = result.fold(
        ifLeft = { SelectInsuranceForChipIdState.Failure },
        ifRight = { contracts ->
          val preselected = contracts.firstOrNull { it.id == preselectedContractId }

          if (contracts.size == 1) {
            contractIdToContinue = contracts[0].id
          }

          SelectInsuranceForChipIdState.Success(
            contracts = contracts,
            selectedContract = preselected,
            contractIdToContinue = contractIdToContinue,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SelectInsuranceForChipIdEvent.Reload -> {
          loadIteration++
        }

        is SelectInsuranceForChipIdEvent.SelectContract -> {
          selectedContract = event.contract
        }

        SelectInsuranceForChipIdEvent.SubmitSelected -> {
          selectedContract?.let { selected ->
            contractIdToContinue = selected.id
          }
        }

        SelectInsuranceForChipIdEvent.ClearNavigation -> {
          contractIdToContinue = null
        }
      }
    }

    return when (val state = currentState) {
      is SelectInsuranceForChipIdState.Success -> {
        state.copy(
          selectedContract = selectedContract ?: state.selectedContract,
          contractIdToContinue = contractIdToContinue,
        )
      }

      else -> {
        state
      }
    }
  }
}

internal sealed interface SelectInsuranceForChipIdState {
  data object Loading : SelectInsuranceForChipIdState

  data class Success(
    val contracts: List<PetContractForChipId>,
    val selectedContract: PetContractForChipId?,
    val contractIdToContinue: String? = null,
  ) : SelectInsuranceForChipIdState

  data object Failure : SelectInsuranceForChipIdState
}

internal sealed interface SelectInsuranceForChipIdEvent {
  data object Reload : SelectInsuranceForChipIdEvent

  data class SelectContract(val contract: PetContractForChipId) : SelectInsuranceForChipIdEvent

  data object SubmitSelected : SelectInsuranceForChipIdEvent

  data object ClearNavigation : SelectInsuranceForChipIdEvent
}
