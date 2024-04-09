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
    var terminatableInsuranceToFetchNextStepFor: TerminatableInsurance? by remember { mutableStateOf(null) }
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

        is ChooseInsuranceToTerminateEvent.SubmitSelectedInsuranceToTerminate -> {
          terminatableInsuranceToFetchNextStepFor = event.insurance
        }

        is ChooseInsuranceToTerminateEvent.SelectInsurance -> {
          val currentStateValue = currentState
          if (currentStateValue is ChooseInsuranceToTerminateStepUiState.Success) {
            currentState = currentStateValue.copy(
              selectedInsurance = event.insurance,
              navigationStepFailedToLoad = false,
            )
          }
        }

        ChooseInsuranceToTerminateEvent.ClearTerminationStep -> {
          val currentStateValue = currentState
          if (currentStateValue is ChooseInsuranceToTerminateStepUiState.Success) {
            currentState = currentStateValue.copy(
              nextStepWithInsurance = null,
              isNavigationStepLoading = false,
            )
          }
        }
      }
    }

    LaunchedEffect(terminatableInsuranceToFetchNextStepFor) {
      val currentStateValue = currentState as? ChooseInsuranceToTerminateStepUiState.Success ?: return@LaunchedEffect
      currentState = currentStateValue.copy(
        navigationStepFailedToLoad = false,
      )
      val terminatableInsurance = terminatableInsuranceToFetchNextStepFor ?: return@LaunchedEffect
      currentState = currentStateValue.copy(isNavigationStepLoading = true)
      currentState = terminateInsuranceRepository
        .startTerminationFlow(InsuranceId(terminatableInsurance.id))
        .fold(
          ifLeft = {
            currentStateValue.copy(
              navigationStepFailedToLoad = true,
              isNavigationStepLoading = false,
            )
          },
          ifRight = { step ->
            val terminationStepWithInsurance = Pair(step, terminatableInsurance)
            currentStateValue.copy(
              nextStepWithInsurance = terminationStepWithInsurance,
              navigationStepFailedToLoad = false,
              isNavigationStepLoading = true,
            )
          },
        )
      terminatableInsuranceToFetchNextStepFor = null
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
            currentState = if (eligibleInsurances == null) {
              ChooseInsuranceToTerminateStepUiState.NotAllowed
            } else {
              val selectedInsurance = if (eligibleInsurances.size == 1) {
                eligibleInsurances[0]
              } else {
                eligibleInsurances.firstOrNull { it.id == initialSelected }
              }
              ChooseInsuranceToTerminateStepUiState.Success(
                eligibleInsurances,
                selectedInsurance,
                null,
                false,
                false,
              )
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

  data class SubmitSelectedInsuranceToTerminate(val insurance: TerminatableInsurance) :
    ChooseInsuranceToTerminateEvent

  data object ClearTerminationStep : ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<TerminatableInsurance>,
    val selectedInsurance: TerminatableInsurance?,
    val nextStepWithInsurance: Pair<TerminateInsuranceStep, TerminatableInsurance>?,
    val isNavigationStepLoading: Boolean,
    val navigationStepFailedToLoad: Boolean,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState

  data object NotAllowed : ChooseInsuranceToTerminateStepUiState
}
