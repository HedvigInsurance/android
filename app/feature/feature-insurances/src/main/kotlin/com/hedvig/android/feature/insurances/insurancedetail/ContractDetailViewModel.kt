package com.hedvig.android.feature.insurances.insurancedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

internal class ContractDetailViewModel(
  contractId: String,
  featureManager: FeatureManager,
  getContractForContractIdUseCase: GetContractForContractIdUseCase,
) : MoleculeViewModel<ContractDetailsEvent, ContractDetailsUiState>(
    initialState = ContractDetailsUiState.Loading,
    presenter = ContractDetailPresenter(contractId, featureManager, getContractForContractIdUseCase),
  )

internal class ContractDetailPresenter(
  private val contractId: String,
  private val featureManager: FeatureManager,
  private val getContractForContractIdUseCase: GetContractForContractIdUseCase,
) :
  MoleculePresenter<ContractDetailsEvent, ContractDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<ContractDetailsEvent>.present(
    lastState: ContractDetailsUiState,
  ): ContractDetailsUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        is ContractDetailsEvent.RetryLoadingContract -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (currentState !is ContractDetailsUiState.Success) {
        currentState = ContractDetailsUiState.Loading
      }
      combine(
        getContractForContractIdUseCase.invoke(contractId),
        featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
      ) { insuranceContractResult, isTerminationFlowEnabled ->
        insuranceContractResult to isTerminationFlowEnabled
      }.collectLatest { (insuranceContractResult, isTerminationFlowEnabled) ->
        insuranceContractResult.fold(
          ifLeft = { error ->
            currentState = when (error) {
              is GetContractForContractIdError.GenericError -> ContractDetailsUiState.Error
              is GetContractForContractIdError.NoContractFound -> ContractDetailsUiState.NoContractFound
            }
          },
          ifRight = { contract ->
            val noTerminationDateYet = contract.terminationDate == null
            currentState = ContractDetailsUiState.Success(
              insuranceContract = contract,
              allowTerminatingInsurance = isTerminationFlowEnabled && noTerminationDateYet,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface ContractDetailsUiState {
  data class Success(
    val insuranceContract: InsuranceContract,
    val allowTerminatingInsurance: Boolean,
  ) : ContractDetailsUiState

  data object Error : ContractDetailsUiState

  data object NoContractFound : ContractDetailsUiState

  data object Loading : ContractDetailsUiState
}

internal interface ContractDetailsEvent {
  data object RetryLoadingContract : ContractDetailsEvent
}
