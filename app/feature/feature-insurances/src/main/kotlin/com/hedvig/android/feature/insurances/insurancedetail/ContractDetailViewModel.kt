package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class ContractDetailViewModel(
  contractId: String,
  private val featureManager: FeatureManager,
  private val getContractForContractIdUseCase: GetContractForContractIdUseCase,
) : ViewModel() {
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    combine(
      getContractForContractIdUseCase.invoke(contractId),
      featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
    ) { insuranceContractResult, isTerminationFlowEnabled ->
      insuranceContractResult.fold(
        ifLeft = { error ->
          when (error) {
            is GetContractForContractIdError.GenericError -> ContractDetailsUiState.Error
            is GetContractForContractIdError.NoContractFound -> ContractDetailsUiState.NoContractFound
          }
        },
        ifRight = { contract ->
          val noTerminationDateYet = contract.terminationDate == null
          // todo  delay(300)
          ContractDetailsUiState.Success(
            insuranceContract = contract,
            allowTerminatingInsurance = isTerminationFlowEnabled && noTerminationDateYet,
          )
        },
      )
    }.collect(this)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    ContractDetailsUiState.Loading,
  )

  fun retryLoadingContract() {
    retryChannel.retry()
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
