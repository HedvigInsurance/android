package com.hedvig.android.feature.chip.id.ui.selectinsurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.chip.id.data.GetContractsWithMissingChipIdUseCase
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import com.hedvig.android.feature.chip.id.navigation.AddChipIdKey
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectInsuranceForChipIdViewModel(
  @Assisted preselectedContractId: String?,
  getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState>(
    initialState = SelectInsuranceForChipIdState.Loading,
    presenter = SelectInsuranceForChipIdPresenter(
      preselectedContractId,
      getContractsWithMissingChipIdUseCase,
      backstack,
    ),
  )

internal class SelectInsuranceForChipIdPresenter(
  private val preselectedContractId: String?,
  private val getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SelectInsuranceForChipIdEvent, SelectInsuranceForChipIdState> {
  @Composable
  override fun MoleculePresenterScope<SelectInsuranceForChipIdEvent>.present(
    lastState: SelectInsuranceForChipIdState,
  ): SelectInsuranceForChipIdState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    var selectedContract: PetContractForChipId? by remember {
      mutableStateOf(
        if (lastState is SelectInsuranceForChipIdState.Success) {
          lastState.selectedContract
        } else {
          null
        },
      )
    }

    LaunchedEffect(loadIteration) {
      currentState = SelectInsuranceForChipIdState.Loading
      val result = getContractsWithMissingChipIdUseCase.invoke()
      currentState = result.fold(
        ifLeft = { SelectInsuranceForChipIdState.Failure },
        ifRight = { contracts ->
          val preselected = contracts.firstOrNull { it.id == preselectedContractId }

          if (contracts.size == 1) {
            navigateToAddChipId(contracts[0].id, popSelectInsurance = true)
          }

          SelectInsuranceForChipIdState.Success(
            contracts = contracts,
            selectedContract = preselected,
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
          val successState = currentState as? SelectInsuranceForChipIdState.Success ?: return@CollectEvents
          selectedContract?.let { selected ->
            navigateToAddChipId(selected.id, popSelectInsurance = successState.contracts.size == 1)
          }
        }
      }
    }

    return when (val state = currentState) {
      is SelectInsuranceForChipIdState.Success -> {
        state.copy(selectedContract = selectedContract ?: state.selectedContract)
      }

      else -> {
        state
      }
    }
  }

  private fun navigateToAddChipId(contractId: String, popSelectInsurance: Boolean) {
    if (popSelectInsurance) {
      backstack.navigateAndPopUpTo<ChipIdKey>(AddChipIdKey(contractId), inclusive = true)
    } else {
      backstack.add(AddChipIdKey(contractId))
    }
  }
}

internal sealed interface SelectInsuranceForChipIdState {
  data object Loading : SelectInsuranceForChipIdState

  data class Success(
    val contracts: List<PetContractForChipId>,
    val selectedContract: PetContractForChipId?,
  ) : SelectInsuranceForChipIdState

  data object Failure : SelectInsuranceForChipIdState
}

internal sealed interface SelectInsuranceForChipIdEvent {
  data object Reload : SelectInsuranceForChipIdEvent

  data class SelectContract(val contract: PetContractForChipId) : SelectInsuranceForChipIdEvent

  data object SubmitSelected : SelectInsuranceForChipIdEvent
}
