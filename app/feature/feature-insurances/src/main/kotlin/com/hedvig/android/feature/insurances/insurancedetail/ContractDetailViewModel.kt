package com.hedvig.android.feature.insurances.insurancedetail

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.navigation.InsurancesDestinations
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.navigation.compose.typedToRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

internal class ContractDetailViewModel(
  val contractId: String,
  @DrawableRes val contractCardDrawableId: Int?,
  featureManager: FeatureManager,
  getContractForContractIdUseCase: GetContractForContractIdUseCase,
) : MoleculeViewModel<ContractDetailsEvent, ContractDetailsUiState>(
    initialState = ContractDetailsUiState.Loading(contractId, contractCardDrawableId),
    presenter = ContractDetailPresenter(
      contractId,
      contractCardDrawableId,
      featureManager,
      getContractForContractIdUseCase,
    ),
  ) {
  companion object {
    operator fun invoke(
      savedStateHandle: SavedStateHandle,
      featureManager: FeatureManager,
      getContractForContractIdUseCase: GetContractForContractIdUseCase,
    ): ContractDetailViewModel {
      val insuranceContractDetail = savedStateHandle.typedToRoute<InsurancesDestinations.InsuranceContractDetail>()
      return ContractDetailViewModel(
        insuranceContractDetail.contractId,
        insuranceContractDetail.contractCardDrawableId,
        featureManager,
        getContractForContractIdUseCase,
      )
    }
  }
}

internal class ContractDetailPresenter(
  private val contractId: String,
  private val insuranceCardImageId: Int?,
  private val featureManager: FeatureManager,
  private val getContractForContractIdUseCase: GetContractForContractIdUseCase,
) : MoleculePresenter<ContractDetailsEvent, ContractDetailsUiState> {
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
        currentState = ContractDetailsUiState.Loading(contractId, insuranceCardImageId)
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

  data class Loading(
    val contractId: String,
    @DrawableRes val insuranceCardImageId: Int?,
  ) : ContractDetailsUiState
}

internal interface ContractDetailsEvent {
  data object RetryLoadingContract : ContractDetailsEvent
}
