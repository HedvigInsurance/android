package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.GetContractsToTerminateUseCase
import com.hedvig.android.feature.terminateinsurance.data.InsuranceForCancellation
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

internal class ChooseInsuranceToTerminateViewModel(
  getContractsToTerminateUseCase: GetContractsToTerminateUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState>(
    initialState = ChooseInsuranceToTerminateStepUiState.Loading,
    presenter = ChooseInsuranceToTerminatePresenter(getContractsToTerminateUseCase, featureManager),
  )

private class ChooseInsuranceToTerminatePresenter(
  private val getContractsToTerminateUseCase: GetContractsToTerminateUseCase,
  private val featureManager: FeatureManager,
) : MoleculePresenter<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToTerminateEvent>.present(
    lastState: ChooseInsuranceToTerminateStepUiState,
  ): ChooseInsuranceToTerminateStepUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        ChooseInsuranceToTerminateEvent.RetryLoadData -> {
          loadIteration++
        }

        is ChooseInsuranceToTerminateEvent.SelectInsurance -> {
          if (currentState is ChooseInsuranceToTerminateStepUiState.Success) {
            currentState =
              (currentState as ChooseInsuranceToTerminateStepUiState.Success).copy(selectedInsurance = event.insurance)
          }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      currentState = ChooseInsuranceToTerminateStepUiState.Loading
      combine(
        flow { emit(getContractsToTerminateUseCase.invoke()) },
        featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
      ) { contractsResult, isTerminationFlowEnabled ->
        if (!isTerminationFlowEnabled) {
          currentState = ChooseInsuranceToTerminateStepUiState.NotAllowed
        } else {
          contractsResult.fold(
            ifLeft = {
              logcat(priority = LogPriority.INFO) { "Cannot load contracts eligible for cancellation" }
              currentState = ChooseInsuranceToTerminateStepUiState.Failure
            },
            ifRight = { eligibleInsurances ->
              logcat(priority = LogPriority.INFO) { "Successfully loaded contracts eligible for cancellation" }
              currentState = if (eligibleInsurances.isNotEmpty()) {
                ChooseInsuranceToTerminateStepUiState.Success(
                  eligibleInsurances,
                  null,
                )
              } else {
                ChooseInsuranceToTerminateStepUiState.NotAllowed
                // todo: if we somehow get an empty list, I think we should show the same
                // "you don't have eligible contracts blablabla" copy
              }
            },
          )
        }
      }
    }
    return currentState
  }
}

internal sealed interface ChooseInsuranceToTerminateEvent {
  data class SelectInsurance(val insurance: InsuranceForCancellation) : ChooseInsuranceToTerminateEvent

  data object RetryLoadData : ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<InsuranceForCancellation>,
    val selectedInsurance: InsuranceForCancellation?,
    val continueEnabled: Boolean = selectedInsurance != null,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}
