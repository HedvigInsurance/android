package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ContractDetailViewModel(
  contractId: String,
  private val featureManager: FeatureManager,
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : ViewModel() {
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    combine(
      getInsuranceContractsUseCaseProvider
        .provide()
        .invoke(forceNetworkFetch = false)
        .map { insuranceContractResult ->
          either {
            val contract = insuranceContractResult.bind().firstOrNull { it.id == contractId }
            ensureNotNull(contract) {
              ErrorMessage("No contract found with id: $contractId").also {
                logcat(LogPriority.ERROR) { it.message.toString() }
              }
            }
          }
        },
      featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
    ) { insuranceContractResult, isTerminationFlowEnabled ->
      insuranceContractResult.fold(
        ifLeft = { ContractDetailsUiState.Error },
        ifRight = { contract ->
          ContractDetailsUiState.Success(
            insuranceContract = contract,
            allowTerminatingInsurance = isTerminationFlowEnabled,
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

  data object Loading : ContractDetailsUiState
}
