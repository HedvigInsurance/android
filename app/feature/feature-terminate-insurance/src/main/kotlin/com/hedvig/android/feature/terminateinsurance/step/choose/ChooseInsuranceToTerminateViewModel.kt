package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseInsuranceToTerminateViewModel(
  insuranceId: String?,
  getTerminatableContractsUseCase: GetTerminatableContractsUseCase,
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState>(
    initialState = ChooseInsuranceToTerminateStepUiState.Loading,
    presenter = ChooseInsuranceToTerminatePresenter(
      insuranceId = insuranceId,
      getTerminatableContractsUseCase = getTerminatableContractsUseCase,
      terminateInsuranceRepository = terminateInsuranceRepository,
    ),
  )

private class ChooseInsuranceToTerminatePresenter(
  private val insuranceId: String?,
  private val getTerminatableContractsUseCase: GetTerminatableContractsUseCase,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToTerminateEvent>.present(
    lastState: ChooseInsuranceToTerminateStepUiState,
  ): ChooseInsuranceToTerminateStepUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    val initialSelected = if (lastState is ChooseInsuranceToTerminateStepUiState.Success) {
      lastState.selectedInsurance?.id
    } else {
      insuranceId
    }
    var currentSelectedId by remember { mutableStateOf(initialSelected) }
    var terminationStep: TerminateInsuranceStep? by remember { mutableStateOf(null) }
    var currentPartialState by remember {
      mutableStateOf(lastState.toPartialState())
    }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceToTerminateEvent.RetryLoadData -> {
          loadIteration++
        }

        is ChooseInsuranceToTerminateEvent.SelectInsurance -> {
          currentSelectedId = event.insurance.id
          if (currentPartialState is PartialUiState.Success) {
            currentPartialState =
              (currentPartialState as PartialUiState.Success).copy(selectedInsurance = event.insurance)
          }
        }
      }
    }

    LaunchedEffect(currentSelectedId) {
      val id = currentSelectedId
      if (id != null) {
        terminationStep = terminateInsuranceRepository.startTerminationFlow(InsuranceId(id)).getOrNull()
      }
    }

    LaunchedEffect(loadIteration) {
      if (lastState !is ChooseInsuranceToTerminateStepUiState.Success) {
        currentPartialState = PartialUiState.Loading
      }
      getTerminatableContractsUseCase.invoke().collect { contractsResult ->
        contractsResult.fold(
          ifLeft = {
            logcat(priority = LogPriority.INFO) { "Cannot load contracts for cancellation" }
            currentPartialState = PartialUiState.Failure
          },
          ifRight = { eligibleInsurances ->
            logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for cancellation" }
            val selectedInsurance = if (currentSelectedId != null) {
              eligibleInsurances?.firstOrNull { it.id == currentSelectedId }
            } else {
              null
            }
            currentPartialState = if (eligibleInsurances != null) {
              PartialUiState.Success(
                eligibleInsurances,
                selectedInsurance,
              )
            } else {
              PartialUiState.NotEligible
            }
          },
        )
      }
    }
    return currentPartialState.toUiState(terminationStep)
  }
}

private sealed interface PartialUiState {
  data object Loading : PartialUiState

  data class Success(
    val insuranceList: List<TerminatableInsurance>,
    val selectedInsurance: TerminatableInsurance?,
  ) : PartialUiState

  data object Failure : PartialUiState

  data object NotEligible : PartialUiState
}

internal sealed interface ChooseInsuranceToTerminateEvent {
  data class SelectInsurance(val insurance: TerminatableInsurance) :
    ChooseInsuranceToTerminateEvent

  data object RetryLoadData : ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<TerminatableInsurance>,
    val selectedInsurance: TerminatableInsurance?,
    val nextStep: TerminateInsuranceStep?,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}

private fun ChooseInsuranceToTerminateStepUiState.toPartialState(): PartialUiState {
  return when (this) {
    ChooseInsuranceToTerminateStepUiState.Failure -> PartialUiState.Failure
    ChooseInsuranceToTerminateStepUiState.Loading -> PartialUiState.Loading
    ChooseInsuranceToTerminateStepUiState.NotAllowed -> PartialUiState.NotEligible
    is ChooseInsuranceToTerminateStepUiState.Success -> PartialUiState.Success(
      insuranceList,
      selectedInsurance,
    )
  }
}

private fun PartialUiState.toUiState(nextStep: TerminateInsuranceStep?): ChooseInsuranceToTerminateStepUiState {
  return when (this) {
    PartialUiState.Failure -> ChooseInsuranceToTerminateStepUiState.Failure
    PartialUiState.Loading -> ChooseInsuranceToTerminateStepUiState.Loading
    PartialUiState.NotEligible -> ChooseInsuranceToTerminateStepUiState.NotAllowed
    is PartialUiState.Success -> ChooseInsuranceToTerminateStepUiState.Success(
      insuranceList,
      selectedInsurance,
      nextStep,
    )
  }
}
