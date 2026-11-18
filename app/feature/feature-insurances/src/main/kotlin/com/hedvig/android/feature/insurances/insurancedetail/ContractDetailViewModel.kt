package com.hedvig.android.feature.insurances.insurancedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.insurances.data.GetGreenCardEligibleContractIdsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurancedetail.GetContractForContractIdUseCaseImpl.GetContractForContractIdError
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ContractDetailViewModel(
  @Assisted contractId: String,
  getContractForContractIdUseCase: GetContractForContractIdUseCase,
  getGreenCardEligibleContractIdsUseCase: GetGreenCardEligibleContractIdsUseCase,
) : MoleculeViewModel<ContractDetailsEvent, ContractDetailsUiState>(
    initialState = ContractDetailsUiState.Loading,
    presenter = ContractDetailPresenter(
      contractId,
      getContractForContractIdUseCase,
      getGreenCardEligibleContractIdsUseCase,
    ),
  )

internal class ContractDetailPresenter(
  private val contractId: String,
  private val getContractForContractIdUseCase: GetContractForContractIdUseCase,
  private val getGreenCardEligibleContractIdsUseCase: GetGreenCardEligibleContractIdsUseCase,
) :
  MoleculePresenter<ContractDetailsEvent, ContractDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<ContractDetailsEvent>.present(
    lastState: ContractDetailsUiState,
  ): ContractDetailsUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var isGreenCardAvailable by remember {
      mutableStateOf((lastState as? ContractDetailsUiState.Success)?.isGreenCardAvailable ?: false)
    }

    CollectEvents { event ->
      when (event) {
        is ContractDetailsEvent.RetryLoadingContract -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (currentState !is ContractDetailsUiState.Success) {
        currentState = ContractDetailsUiState.Loading
      }
      getContractForContractIdUseCase.invoke(contractId).collect { insuranceContractResult ->
        insuranceContractResult.fold(
          ifLeft = { error ->
            currentState = when (error) {
              is GetContractForContractIdError.GenericError -> ContractDetailsUiState.Error
              is GetContractForContractIdError.NoContractFound -> ContractDetailsUiState.NoContractFound
            }
          },
          ifRight = { contract ->
            currentState = ContractDetailsUiState.Success(
              insuranceContract = contract,
              allowTerminatingInsurance = contract.supportsTermination,
            )
          },
        )
      }
    }
    LaunchedEffect(dataLoadIteration) {
      getGreenCardEligibleContractIdsUseCase.invoke().onRight { eligibleContractIds ->
        isGreenCardAvailable = contractId in eligibleContractIds
      }
    }

    return when (val state = currentState) {
      is ContractDetailsUiState.Success -> state.copy(isGreenCardAvailable = isGreenCardAvailable)
      else -> state
    }
  }
}

internal sealed interface ContractDetailsUiState {
  data class Success(
    val insuranceContract: InsuranceContract,
    val allowTerminatingInsurance: Boolean,
    val isGreenCardAvailable: Boolean = false,
  ) : ContractDetailsUiState

  data object Error : ContractDetailsUiState

  data object NoContractFound : ContractDetailsUiState

  data object Loading : ContractDetailsUiState
}

internal interface ContractDetailsEvent {
  data object RetryLoadingContract : ContractDetailsEvent
}
