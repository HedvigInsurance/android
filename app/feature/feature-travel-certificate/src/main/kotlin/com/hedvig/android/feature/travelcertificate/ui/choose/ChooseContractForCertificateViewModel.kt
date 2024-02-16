package com.hedvig.android.feature.travelcertificate.ui.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.travelcertificate.ContractEligibleWithAddress
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseContractForCertificateViewModel(
  getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
) : MoleculeViewModel<ChooseContractEvent, ChooseContractUiState>(
    initialState = ChooseContractUiState.Loading,
    presenter = ChooseContractPresenter(getEligibleContractsWithAddressUseCase),
  )

internal class ChooseContractPresenter(
  private val getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
) : MoleculePresenter<ChooseContractEvent, ChooseContractUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseContractEvent>.present(
    lastState: ChooseContractUiState,
  ): ChooseContractUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        ChooseContractEvent.RetryLoadData -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is ChooseContractUiState.Success) {
        return@LaunchedEffect
      }
      getEligibleContractsWithAddressUseCase.invoke().fold(
        ifRight = { list ->
          logcat(priority = LogPriority.INFO) { "Successfully loaded contracts eligible for travel certificates" }
          currentState = ChooseContractUiState.Success(list)
        },
        ifLeft = {
          logcat(priority = LogPriority.INFO) { "Cannot load contracts eligible for travel certificates" }
          currentState = ChooseContractUiState.Failure
        },
      )
    }
    return currentState
  }
}

internal sealed interface ChooseContractEvent {
  data object RetryLoadData : ChooseContractEvent
}

internal sealed interface ChooseContractUiState {
  data object Loading : ChooseContractUiState

  data object Failure : ChooseContractUiState

  data class Success(val eligibleContracts: List<ContractEligibleWithAddress>) : ChooseContractUiState
}
