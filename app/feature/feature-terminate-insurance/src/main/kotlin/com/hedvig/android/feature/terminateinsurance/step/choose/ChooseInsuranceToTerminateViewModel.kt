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
    var terminationStepLoadIteration by remember { mutableIntStateOf(0) }
    val initialSelected = if (lastState is ChooseInsuranceToTerminateStepUiState.Success) {
      lastState.selectedInsurance?.id
    } else {
      insuranceId
    }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceToTerminateEvent.RetryLoadData -> {
          loadIteration++
        }

        ChooseInsuranceToTerminateEvent.FetchTerminationStep -> {
          terminationStepLoadIteration++
        }

        is ChooseInsuranceToTerminateEvent.SelectInsurance -> {
          if (currentState is ChooseInsuranceToTerminateStepUiState.Success) {
            currentState =
              (currentState as ChooseInsuranceToTerminateStepUiState.Success).copy(selectedInsurance = event.insurance)
          }
        }

        ChooseInsuranceToTerminateEvent.ClearTerminationStep -> {
          if (currentState is ChooseInsuranceToTerminateStepUiState.Success) {
            currentState =
              (currentState as ChooseInsuranceToTerminateStepUiState.Success).copy(
                nextStepWithInsurance = null,
                isNavigationStepLoading = false,
              )
          }
        }
      }
    }

    LaunchedEffect(terminationStepLoadIteration) {
      val previousState = currentState
      if (previousState is ChooseInsuranceToTerminateStepUiState.Success &&
        terminationStepLoadIteration != 0 &&
        previousState.selectedInsurance?.id != null
      ) {
        currentState = previousState.copy(isNavigationStepLoading = true)
        val id = previousState.selectedInsurance.id
        val step = terminateInsuranceRepository.startTerminationFlow(InsuranceId(id)).getOrNull()
        currentState = if (step == null) {
          previousState
        } else {
          val terminationStepWithInsurance = Pair(step, previousState.selectedInsurance)
          previousState.copy(nextStepWithInsurance = terminationStepWithInsurance, isNavigationStepLoading = true)
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (lastState !is ChooseInsuranceToTerminateStepUiState.Success) {
        currentState = ChooseInsuranceToTerminateStepUiState.Loading
      }
      getTerminatableContractsUseCase.invoke().collect { contractsResult ->
        contractsResult.fold(
          ifLeft = {
            logcat(priority = LogPriority.INFO) { "Cannot load contracts for cancellation" }
            currentState = ChooseInsuranceToTerminateStepUiState.Failure
          },
          ifRight = { eligibleInsurances ->
            logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for cancellation" }
            val selectedInsurance = if (initialSelected != null) {
              eligibleInsurances?.firstOrNull { it.id == initialSelected }
            } else if (eligibleInsurances?.size == 1) {
              eligibleInsurances[0]
            } else {
              null
            }
            currentState = if (eligibleInsurances != null) {
              ChooseInsuranceToTerminateStepUiState.Success(
                eligibleInsurances,
                selectedInsurance,
                null,
                false,
              )
            } else {
              ChooseInsuranceToTerminateStepUiState.NotAllowed
            }
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface ChooseInsuranceToTerminateEvent {
  data class SelectInsurance(val insurance: TerminatableInsurance) :
    ChooseInsuranceToTerminateEvent

  data object RetryLoadData : ChooseInsuranceToTerminateEvent

  data object FetchTerminationStep : ChooseInsuranceToTerminateEvent

  data object ClearTerminationStep : ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<TerminatableInsurance>,
    val selectedInsurance: TerminatableInsurance?,
    val nextStepWithInsurance: Pair<TerminateInsuranceStep, TerminatableInsurance>?,
    val isNavigationStepLoading: Boolean,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}
