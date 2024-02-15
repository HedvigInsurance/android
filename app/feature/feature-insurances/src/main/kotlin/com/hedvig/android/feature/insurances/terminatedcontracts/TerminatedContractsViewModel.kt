package com.hedvig.android.feature.insurances.terminatedcontracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

internal class TerminatedContractsViewModel(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : ViewModel() {
  private val retryChannel = RetryChannel()

  val uiState: StateFlow<TerminatedContractsUiState> = flow {
    emit(TerminatedContractsUiState.Loading)
    getInsuranceContractsUseCaseProvider
      .provide()
      .invoke(forceNetworkFetch = false)
      .onEach { result: Either<ErrorMessage, List<InsuranceContract>> ->
        result.onLeft { errorMessage ->
          logcat(LogPriority.INFO, errorMessage.throwable) {
            "Terminated contracts failed to load ${errorMessage.message}"
          }
        }
      }
      .map { insuranceContractResult ->
        either {
          val terminatedContracts = insuranceContractResult.bind().filter(InsuranceContract::isTerminated)
          ensure(terminatedContracts.isNotEmpty()) {
            ErrorMessage("", NoSuchElementException())
          }
          if (terminatedContracts.isEmpty()) {
            logcat(LogPriority.ERROR) { "Terminated insurances screen got 0 terminated insurances" }
            TerminatedContractsUiState.NoTerminatedInsurances
          } else {
            TerminatedContractsUiState.Success(terminatedContracts.toImmutableList())
          }
        }.fold(
          ifLeft = { errorMessage ->
            TerminatedContractsUiState.Error
          },
          ifRight = { it },
        )
      }
      .collect(this)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    TerminatedContractsUiState.Loading,
  )

  fun retry() {
    if (uiState.value is TerminatedContractsUiState.Error) {
      retryChannel.retry()
    }
  }
}

internal sealed interface TerminatedContractsUiState {
  data class Success(
    val insuranceContracts: ImmutableList<InsuranceContract>,
  ) : TerminatedContractsUiState

  data object NoTerminatedInsurances : TerminatedContractsUiState

  data object Loading : TerminatedContractsUiState

  data object Error : TerminatedContractsUiState
}
