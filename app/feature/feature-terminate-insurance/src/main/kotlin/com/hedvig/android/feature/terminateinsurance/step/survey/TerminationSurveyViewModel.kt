package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TerminationSurveyViewModel(
  options: List<TerminationSurveyOption>,
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
    initialState = TerminationSurveyState(),
    presenter = TerminationSurveyPresenter(options, terminateInsuranceRepository),
  )

internal class TerminationSurveyPresenter(
  private val options: List<TerminationSurveyOption>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(
    lastState: TerminationSurveyState,
  ): TerminationSurveyState {
    var loadNextStep by remember { mutableStateOf(false) }

    val initialReasons = (
      lastState.reasons.ifEmpty { // in initial state reasons are empty, so we take them from parameters
        options.map { option ->
          TerminationReason(option, null)
        }
      }
    ).map {
      it.surveyOption to it.feedBack
    }
    val currentReasonsWithFeedback = remember {
      mutableStateMapOf(*initialReasons.toTypedArray())
    }

    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is TerminationSurveyEvent.ChangeFeedbackForReason -> {
          currentReasonsWithFeedback[event.option] = event.newFeedback
        }

        is TerminationSurveyEvent.SelectOption -> {
          currentState = currentState.copy(selectedOption = event.option)
        }

        is TerminationSurveyEvent.Continue -> {
          val state = currentState
          val selectedOption = state.selectedOption ?: return@CollectEvents
          if (selectedOption.subOptions.isNotEmpty()) {
            currentState = state.copy(nextNavigationStep = SurveyNavigationStep.NavigateToSubOptions)
          } else {
            loadNextStep = true
          }
        }

        TerminationSurveyEvent.ClearNextStep -> {
          currentState = currentState.copy(nextNavigationStep = null)
        }
      }
    }

    LaunchedEffect(loadNextStep) {
      if (loadNextStep) {
        val option = currentState.selectedOption ?: return@LaunchedEffect
        currentState = currentState.copy(isNavigationStepLoading = true)
        val reason = TerminationReason(option, currentReasonsWithFeedback[option])
        currentState = terminateInsuranceRepository
          .submitReasonForCancelling(reason)
          .fold(
            ifLeft = {
              loadNextStep = false
              currentState.copy(
                isNavigationStepLoading = false,
                errorWhileLoadingNextStep = true,
              )
            },
            ifRight = { step ->
              loadNextStep = false
              currentState.copy(
                isNavigationStepLoading = false,
                errorWhileLoadingNextStep = false,
                nextNavigationStep = SurveyNavigationStep.NavigateToNextTerminationStep(step),
              )
            },
          )
      }
    }

    return currentState.copy(
      reasons = currentReasonsWithFeedback.map {
        TerminationReason(it.key, it.value)
      },
    )
  }
}

internal sealed interface TerminationSurveyEvent {
  data class SelectOption(val option: TerminationSurveyOption) : TerminationSurveyEvent

  data object Continue : TerminationSurveyEvent

  data class ChangeFeedbackForReason(
    val option: TerminationSurveyOption,
    val newFeedback: String?,
  ) : TerminationSurveyEvent

  data object ClearNextStep : TerminationSurveyEvent
}

internal data class TerminationSurveyState(
  val reasons: List<TerminationReason> = listOf(),
  val selectedOption: TerminationSurveyOption? = null,
  val nextNavigationStep: SurveyNavigationStep? = null,
  val isNavigationStepLoading: Boolean = false,
  val continueAllowed: Boolean = selectedOption != null,
  val errorWhileLoadingNextStep: Boolean = false,
)

internal sealed interface SurveyNavigationStep {
  data class NavigateToNextTerminationStep(val step: TerminateInsuranceStep) : SurveyNavigationStep

  data object NavigateToSubOptions : SurveyNavigationStep
}
