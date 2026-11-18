package com.hedvig.android.feature.change.mileage.ui.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.change.mileage.data.GetMileageContractsUseCase
import com.hedvig.android.feature.change.mileage.data.MileageContract
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageChooseInsuranceKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ChooseInsuranceForMileageViewModel(
  getMileageContractsUseCase: GetMileageContractsUseCase,
  backstack: Backstack,
) : MoleculeViewModel<ChooseInsuranceForMileageEvent, ChooseInsuranceForMileageUiState>(
    initialState = ChooseInsuranceForMileageUiState.Loading,
    presenter = ChooseInsuranceForMileagePresenter(getMileageContractsUseCase, backstack),
  )

internal class ChooseInsuranceForMileagePresenter(
  private val getMileageContractsUseCase: GetMileageContractsUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<ChooseInsuranceForMileageEvent, ChooseInsuranceForMileageUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceForMileageEvent>.present(
    lastState: ChooseInsuranceForMileageUiState,
  ): ChooseInsuranceForMileageUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceForMileageEvent.Reload -> {
          loadIteration++
        }

        is ChooseInsuranceForMileageEvent.SelectInsurance -> {
          val state = currentState as? ChooseInsuranceForMileageUiState.Success ?: return@CollectEvents
          currentState = state.copy(selectedContractId = event.contractId)
        }

        ChooseInsuranceForMileageEvent.Continue -> {
          val state = currentState as? ChooseInsuranceForMileageUiState.Success ?: return@CollectEvents
          val contractId = state.selectedContractId ?: return@CollectEvents
          startFlowFor(contractId)
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (loadIteration == 0 && lastState is ChooseInsuranceForMileageUiState.Success) return@LaunchedEffect
      currentState = ChooseInsuranceForMileageUiState.Loading
      getMileageContractsUseCase.invoke().fold(
        ifLeft = { currentState = ChooseInsuranceForMileageUiState.Failure },
        ifRight = { contracts ->
          when (contracts.size) {
            0 -> currentState = ChooseInsuranceForMileageUiState.NoEligibleInsurances
            1 -> startFlowFor(contracts.single().id)
            else -> currentState = ChooseInsuranceForMileageUiState.Success(contracts, selectedContractId = null)
          }
        },
      )
    }

    return currentState
  }

  private fun startFlowFor(contractId: String) {
    backstack.navigateAndPopUpTo<ChangeMileageChooseInsuranceKey>(ChangeMileageKey(contractId), inclusive = true)
  }
}

internal sealed interface ChooseInsuranceForMileageEvent {
  data class SelectInsurance(val contractId: String) : ChooseInsuranceForMileageEvent

  data object Continue : ChooseInsuranceForMileageEvent

  data object Reload : ChooseInsuranceForMileageEvent
}

internal sealed interface ChooseInsuranceForMileageUiState {
  data object Loading : ChooseInsuranceForMileageUiState

  data object Failure : ChooseInsuranceForMileageUiState

  data object NoEligibleInsurances : ChooseInsuranceForMileageUiState

  data class Success(
    val contracts: List<MileageContract>,
    val selectedContractId: String?,
  ) : ChooseInsuranceForMileageUiState
}
