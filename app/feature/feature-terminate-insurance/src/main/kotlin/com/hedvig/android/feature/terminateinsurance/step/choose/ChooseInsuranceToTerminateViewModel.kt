package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.GetContractsToTerminateUseCase
import com.hedvig.android.feature.terminateinsurance.data.InsuranceForCancellation
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
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
  insuranceId: String?,
  getContractsToTerminateUseCase: GetContractsToTerminateUseCase,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  featureManager: FeatureManager,
) : MoleculeViewModel<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState>(
  initialState = ChooseInsuranceToTerminateStepUiState.Loading,
  presenter = ChooseInsuranceToTerminatePresenter(
    insuranceId = insuranceId,
    getContractsToTerminateUseCase = getContractsToTerminateUseCase,
    terminateInsuranceRepository = terminateInsuranceRepository,
    featureManager = featureManager,
  ),
)

private class ChooseInsuranceToTerminatePresenter(
  private val insuranceId: String?,
  private val getContractsToTerminateUseCase: GetContractsToTerminateUseCase,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val featureManager: FeatureManager,
) : MoleculePresenter<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToTerminateEvent>.present(
    lastState: ChooseInsuranceToTerminateStepUiState,
  ): ChooseInsuranceToTerminateStepUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentSelectedId by remember { mutableStateOf(insuranceId) }
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
        logcat { "mariia: LaunchedEffect(currentSelectedId) finished, terminationStep: $terminationStep" }
      }
    }

    LaunchedEffect(loadIteration) {
      currentPartialState = PartialUiState.Loading
      combine(
        flow { emit(getContractsToTerminateUseCase.invoke()) },
        featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW),
      ) { contractsResult, isTerminationFlowEnabled ->
        contractsResult to isTerminationFlowEnabled
      }.collect { (contractsResult, isTerminationFlowEnabled) ->
        logcat { "mariia: contractsResult: $contractsResult isTerminationFlowEnabled: $isTerminationFlowEnabled" }

        if (!isTerminationFlowEnabled) {
          currentPartialState = PartialUiState.NotAllowed
        } else {
          contractsResult.fold(
            ifLeft = {
              logcat(priority = LogPriority.INFO) { "Cannot load contracts for cancellation" }
              logcat { "mariia: LaunchedEffect(loadIteration) Cannot load contracts for cancellation" }
              currentPartialState = PartialUiState.Failure
            },
            ifRight = { eligibleInsurances ->
              logcat { "mariia: LaunchedEffect(loadIteration) Successfully loaded contracts for cancellation" }
              logcat(priority = LogPriority.INFO) { "Successfully loaded contracts for cancellation" }
              val selectedInsurance = if (insuranceId != null) {
                eligibleInsurances.firstOrNull { it.id == insuranceId }
              } else {
                null
              }
              currentPartialState = if (eligibleInsurances.isNotEmpty()) {
                PartialUiState.Success(
                  eligibleInsurances,
                  selectedInsurance,
                )
              } else {
                PartialUiState.NotAllowed
                // todo: if we somehow get an empty list, I think we should show the same
                // "you don't have eligible contracts blablabla" copy
              }
            },
          )
        }
      }
    }
    return currentPartialState.toUiState(terminationStep)
  }
}

private sealed interface PartialUiState {
  data object Loading : PartialUiState

  data class Success(
    val insuranceList: List<InsuranceForCancellation>,
    val selectedInsurance: InsuranceForCancellation?,
  ) : PartialUiState

  data object Failure : PartialUiState

  data object NotAllowed : PartialUiState
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
    val nextStep: TerminateInsuranceStep?,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}

private fun ChooseInsuranceToTerminateStepUiState.toPartialState(): PartialUiState {
  return when (this) {
    ChooseInsuranceToTerminateStepUiState.Failure -> PartialUiState.Failure
    ChooseInsuranceToTerminateStepUiState.Loading -> PartialUiState.Loading
    ChooseInsuranceToTerminateStepUiState.NotAllowed -> PartialUiState.NotAllowed
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
    PartialUiState.NotAllowed -> ChooseInsuranceToTerminateStepUiState.NotAllowed
    is PartialUiState.Success -> ChooseInsuranceToTerminateStepUiState.Success(
      insuranceList,
      selectedInsurance,
      nextStep,
    )
  }
}
