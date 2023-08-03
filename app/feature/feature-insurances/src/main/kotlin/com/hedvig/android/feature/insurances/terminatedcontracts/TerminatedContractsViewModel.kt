package com.hedvig.android.feature.insurances.terminatedcontracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class TerminatedContractsViewModel(
  private val getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
) : ViewModel() {
  private val retryChannel = RetryChannel()

  val uiState: StateFlow<TerminatedContractsUiState> = flow {
    emit(TerminatedContractsUiState.Loading)
    either {
      val terminatedContracts = getInsuranceContractsUseCase
        .invoke()
        .bind()
        .filter(InsuranceContract::isTerminated)
      ensure(terminatedContracts.isNotEmpty()) {
        ErrorMessage("", NoSuchElementException())
      }
      if (terminatedContracts.isEmpty()) {
        e { "Terminated insurances screen got 0 terminated insurances" }
        TerminatedContractsUiState.NoTerminatedInsurances
      } else {
        TerminatedContractsUiState.Success(
          terminatedContracts.map { contract ->
            TerminatedContractsUiState.Success.InsuranceCard(
              contractId = contract.id,
              chips = contract.statusPills.toPersistentList(),
              title = contract.displayName,
              subtitle = contract.detailPills.joinToString(" ∙ "),
            )
          }.toPersistentList(),
        )
      }
    }.fold(
      ifLeft = { errorMessage ->
        logcat(LogPriority.INFO, errorMessage.throwable) {
          "Terminated contracts failed to load ${errorMessage.message}"
        }
        emit(TerminatedContractsUiState.Error)
      },
      ifRight = { emit(it) },
    )
  }
    .stateIn(
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
    val terminatedInsuranceCards: ImmutableList<InsuranceCard>,
  ) : TerminatedContractsUiState {
    data class InsuranceCard(
      val contractId: String,
      val chips: ImmutableList<String>,
      val title: String,
      val subtitle: String,
    )
  }

  object NoTerminatedInsurances : TerminatedContractsUiState
  object Loading : TerminatedContractsUiState
  object Error : TerminatedContractsUiState
}
