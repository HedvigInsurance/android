package com.hedvig.android.feature.terminateinsurance.step.chooseaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.terminateinsurance.data.ContractWithdrawalRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveyFirstStepKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationWithdrawalKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@HedvigViewModel(ActivityRetainedScope::class)
internal class TerminationChooseActionViewModel @AssistedInject constructor(
  @Assisted commonParams: TerminationGraphParameters,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  contractWithdrawalRepository: ContractWithdrawalRepository,
  backstack: Backstack,
) : MoleculeViewModel<TerminationChooseActionEvent, TerminationChooseActionUiState>(
    initialState = TerminationChooseActionUiState(
      selectedAction = null,
      isNavigationStepLoading = false,
      navigationStepFailedToLoad = false,
    ),
    presenter = TerminationChooseActionPresenter(
      commonParams = commonParams,
      terminateInsuranceRepository = terminateInsuranceRepository,
      contractWithdrawalRepository = contractWithdrawalRepository,
      backstack = backstack,
    ),
  )

internal class TerminationChooseActionPresenter(
  private val commonParams: TerminationGraphParameters,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val contractWithdrawalRepository: ContractWithdrawalRepository,
  private val backstack: Backstack,
) : MoleculePresenter<TerminationChooseActionEvent, TerminationChooseActionUiState> {
  @Composable
  override fun MoleculePresenterScope<TerminationChooseActionEvent>.present(
    lastState: TerminationChooseActionUiState,
  ): TerminationChooseActionUiState {
    var currentState by remember { mutableStateOf(lastState.copy(isNavigationStepLoading = false)) }
    var actionToLoad by remember { mutableStateOf<ActionLoadRequest?>(null) }

    CollectEvents { event ->
      when (event) {
        is TerminationChooseActionEvent.SelectAction -> {
          currentState = currentState.copy(selectedAction = event.action, navigationStepFailedToLoad = false)
        }

        TerminationChooseActionEvent.Continue -> {
          val selectedAction = currentState.selectedAction ?: return@CollectEvents
          if (currentState.isNavigationStepLoading) return@CollectEvents
          actionToLoad = ActionLoadRequest(selectedAction)
        }
      }
    }

    LaunchedEffect(actionToLoad) {
      val request = actionToLoad ?: return@LaunchedEffect
      currentState = currentState.copy(isNavigationStepLoading = true, navigationStepFailedToLoad = false)
      val nextStep = when (request.action) {
        TerminationChooseAction.Withdraw -> {
          contractWithdrawalRepository.getContractWithdrawal(commonParams.contractId).map { withdrawal ->
            TerminationWithdrawalKey(
              commonParams = commonParams,
              statusText = withdrawal.statusText,
              action = withdrawal.action,
            )
          }
        }

        TerminationChooseAction.Terminate -> {
          terminateInsuranceRepository.getTerminationSurvey(commonParams.contractId).map { surveyData ->
            TerminationSurveyFirstStepKey(
              options = surveyData.options,
              action = surveyData.action,
              commonParams = commonParams,
            )
          }
        }
      }
      nextStep.fold(
        ifLeft = {
          currentState = currentState.copy(isNavigationStepLoading = false, navigationStepFailedToLoad = true)
        },
        ifRight = { key ->
          backstack.add(key)
          currentState = currentState.copy(isNavigationStepLoading = false)
        },
      )
    }

    return currentState
  }
}

/**
 * Identity equality, so continuing again with the same action after a failed load starts a new load.
 */
private class ActionLoadRequest(val action: TerminationChooseAction)

internal enum class TerminationChooseAction {
  Withdraw,
  Terminate,
}

internal sealed interface TerminationChooseActionEvent {
  data class SelectAction(val action: TerminationChooseAction) : TerminationChooseActionEvent

  data object Continue : TerminationChooseActionEvent
}

internal data class TerminationChooseActionUiState(
  val selectedAction: TerminationChooseAction?,
  val isNavigationStepLoading: Boolean,
  val navigationStepFailedToLoad: Boolean,
)
