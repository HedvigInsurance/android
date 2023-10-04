package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn

internal class ContractDetailViewModel(
  contractId: String,
  private val getInsuranceContractsUseCase: Provider<GetInsuranceContractsUseCase>,
) : ViewModel() {
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    val uiState = either {
      getInsuranceContractsUseCase
        .provide()
        .invoke(forceNetworkFetch = false)
        .bind()
        .first { it.id == contractId }
    }.fold(
      ifLeft = { ContractDetailsUiState.Error },
      ifRight = { ContractDetailsUiState.Success(it) },
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
  ) : ContractDetailsUiState

  data object Error : ContractDetailsUiState
  data object Loading : ContractDetailsUiState
}
