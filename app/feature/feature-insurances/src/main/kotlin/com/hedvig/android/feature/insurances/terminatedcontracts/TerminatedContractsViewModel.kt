package com.hedvig.android.feature.insurances.terminatedcontracts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach

internal class TerminatedContractsViewModel(
  getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : MoleculeViewModel<TerminatedContractsEvent, TerminatedContractsUiState>(
    initialState = TerminatedContractsUiState.Loading,
    presenter = TerminatedContractsPresenter(getInsuranceContractsUseCaseProvider),
  )

internal class TerminatedContractsPresenter(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
) : MoleculePresenter<TerminatedContractsEvent, TerminatedContractsUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminatedContractsEvent>.present(
    lastState: TerminatedContractsUiState,
  ): TerminatedContractsUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        TerminatedContractsEvent.Retry -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (currentState !is TerminatedContractsUiState.Success) {
        currentState = TerminatedContractsUiState.Loading
      }
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
        .collectLatest { insuranceContractResult ->
          either {
            val terminatedContracts = insuranceContractResult.bind().filter(InsuranceContract::isTerminated)
            if (terminatedContracts.isEmpty()) {
              logcat(LogPriority.ERROR) { "Terminated insurances screen got 0 terminated insurances" }
              TerminatedContractsUiState.NoTerminatedInsurances
            } else {
              TerminatedContractsUiState.Success(terminatedContracts.toImmutableList())
            }
          }.fold(
            ifLeft = { errorMessage ->
              currentState = TerminatedContractsUiState.Error
            },
            ifRight = { uiState ->
              currentState = uiState
            },
          )
        }
    }
    return currentState
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

internal sealed interface TerminatedContractsEvent {
  data object Retry : TerminatedContractsEvent
}
