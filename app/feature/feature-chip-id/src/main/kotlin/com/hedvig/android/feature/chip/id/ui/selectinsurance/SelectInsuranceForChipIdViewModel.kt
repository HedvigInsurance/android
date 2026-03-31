package com.hedvig.android.feature.chip.id.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.chip.id.data.GetContractsWithMissingChipIdUseCase
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectInsuranceForChipIdViewModel(
  preselectedContractId: String?,
  getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
) : MoleculeViewModel<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState>(
    initialState = SelectInsuranceForChipIdState.Loading,
    presenter = SelectInsuranceForChipIdPresenter(preselectedContractId, getContractsWithMissingChipIdUseCase),
  )

internal class SelectInsuranceForChipIdPresenter(
  private val preselectedContractId: String?,
  private val getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
) : MoleculePresenter<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForChipIdEvent>.present(
    lastState: SelectInsuranceForChipIdState,
  ): SelectInsuranceForChipIdState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    var selectedContract: PetContractForChipId? by remember { mutableStateOf(
      if (lastState is SelectInsuranceForChipIdState.Success) lastState.selectedContract else
      null) }
    var contractIdToContinue: String? by remember { mutableStateOf(null) }

    LaunchedEffect(loadIteration) {
      currentState = SelectInsuranceForChipIdState.Loading
      val result = getContractsWithMissingChipIdUseCase.invoke()
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
