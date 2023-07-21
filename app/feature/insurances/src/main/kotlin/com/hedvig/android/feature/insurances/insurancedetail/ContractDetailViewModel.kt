package com.hedvig.android.feature.insurances.insurancedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class ContractDetailViewModel(
  contractId: String,
  private val getContractDetailsUseCase: GetContractDetailsUseCase,
  hAnalytics: HAnalytics,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.INSURANCE_DETAIL)
  }

  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ContractDetailsUiState> = retryChannel.transformLatest {
    emit(ContractDetailsUiState.Loading)
    val uiState = either {
      getContractDetailsUseCase.invoke(contractId).bind()
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
