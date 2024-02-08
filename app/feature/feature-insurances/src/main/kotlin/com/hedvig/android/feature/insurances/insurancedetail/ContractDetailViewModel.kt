package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

internal class ContractDetailViewModel(
  contractId: String,
  private val featureManager: FeatureManager,
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : ViewModel() {
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    val uiState = either {
      val contract = getInsuranceContractsUseCaseProvider
        .provide()
        .invoke(forceNetworkFetch = false)
        .bind()
        .firstOrNull { it.id == contractId }
      ensureNotNull(contract) {
        logcat(LogPriority.ERROR) { "No contract found with id: $contractId" }
        ContractDetailsUiState.Error
      }
    }.fold(
      ifLeft = { ContractDetailsUiState.Error },
      ifRight = {
        val allowTerminationFlow = featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW).first()
        ContractDetailsUiState.Success(it, allowTerminationFlow)
      },
    )
    emit(uiState)
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
