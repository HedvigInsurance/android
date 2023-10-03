package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.insurancedetail.data.ContractDetails
import com.hedvig.android.feature.insurances.insurancedetail.data.GetContractDetailsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class ContractDetailViewModel(
  contractId: String,
  private val getContractDetailsUseCase: Provider<GetContractDetailsUseCase>,
) : ViewModel() {
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    val uiState = either {
      getContractDetailsUseCase.provide().invoke(contractId).bind()
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
    val contractDetails: ContractDetails,
  ) : ContractDetailsUiState

  object Error : ContractDetailsUiState
  object Loading : ContractDetailsUiState
}
