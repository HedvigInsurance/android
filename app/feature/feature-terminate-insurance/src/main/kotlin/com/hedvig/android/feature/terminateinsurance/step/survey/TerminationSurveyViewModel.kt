package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.computations.nullable
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateStepUiState
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TerminationSurveyViewModel(
  options: List<TerminationSurveyOption>,
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
  initialState = TerminationSurveyState.Loading,
  presenter = TerminationSurveyPresenter(options, terminateInsuranceRepository),
)

internal class TerminationSurveyPresenter(
  private val options: List<TerminationSurveyOption>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(lastState: TerminationSurveyState)
    : TerminationSurveyState {

    var loadNextStep by remember { mutableStateOf(false) }

    val initialReasons = (if (lastState is TerminationSurveyState.ShowSurvey) {
      lastState.reasons
    } else {
      options.map { option ->
        TerminationReason(option, null)
      }
    }).map {
      it.surveyOption to it.feedBack
    }
    val currentReasonsWithFeedback = remember {
      mutableStateMapOf(*initialReasons.toTypedArray())
    }

    val initialSelected = if (lastState is TerminationSurveyState.ShowSurvey) {
      lastState.selectedOption
    } else {
      null
    }
    var selectedOption by remember {
      mutableStateOf(initialSelected)
    }

    val initialPartialState = when (lastState) {
      TerminationSurveyState.ErrorWhileSubmittingReason -> PartialUiState.Loading //todo: sure about this?
      TerminationSurveyState.Loading -> PartialUiState.Loading
      is TerminationSurveyState.ShowSurvey -> PartialUiState.ShowSurvey()
    }
    var currentPartialState by remember {
      mutableStateOf(initialPartialState)
    }

    var currentFeedbackEmptyWarning by remember {
      mutableStateOf(false)
    }

    CollectEvents { event ->
      when (event) {
        is TerminationSurveyEvent.ChangeFeedbackForReason -> {
          currentFeedbackEmptyWarning = false
          currentReasonsWithFeedback[event.option] = event.newFeedback
        }

        is TerminationSurveyEvent.SelectOption -> {
          currentFeedbackEmptyWarning = false
          selectedOption = event.option
        }

        TerminationSurveyEvent.Retry -> {
          currentPartialState = PartialUiState.ShowSurvey()
        }

        is TerminationSurveyEvent.Continue -> {
          if (selectedOption?.subOptions?.isNotEmpty() == true) {
            if (currentPartialState !is PartialUiState.ShowSurvey) return@CollectEvents
            currentPartialState = PartialUiState.ShowSurvey(navigateToSubOptions = true)
          } else if (selectedOption?.feedBackRequired==true && currentReasonsWithFeedback[selectedOption] == null) {
            currentFeedbackEmptyWarning = true
          } else {
            loadNextStep = true
          }
        }

        TerminationSurveyEvent.ClearNextStep -> { currentPartialState = PartialUiState.ShowSurvey() }
      }
    }

    LaunchedEffect(loadNextStep) {
      if (loadNextStep) {
        if (currentPartialState !is PartialUiState.ShowSurvey) return@LaunchedEffect
        val option = selectedOption ?: return@LaunchedEffect
        currentPartialState = PartialUiState.ShowSurvey(isNavigationStepLoading = true)
        val reason = TerminationReason(option, currentReasonsWithFeedback[option])
        currentPartialState = terminateInsuranceRepository
          .submitReasonForCancelling(reason)
          .fold(
            ifLeft = {
              loadNextStep = false
              PartialUiState.Error
            },
            ifRight = { step ->
              loadNextStep = false
              PartialUiState.ShowSurvey(nextStep = step)
            },
          )
      }
    }

    return when (val state = currentPartialState) {
      PartialUiState.Error -> {
        TerminationSurveyState.ErrorWhileSubmittingReason
      }

      PartialUiState.Loading -> {
        TerminationSurveyState.Loading
      }

      is PartialUiState.ShowSurvey -> {
        TerminationSurveyState.ShowSurvey(
          reasons = currentReasonsWithFeedback.map {
            TerminationReason(it.key, it.value)
          },
          selectedOption = selectedOption,
          nextStep = state.nextStep,
          navigateToSubOptions = state.navigateToSubOptions,
          feedbackEmptyWarning = currentFeedbackEmptyWarning,
          isNavigationStepLoading = state.isNavigationStepLoading
        )
      }
    }
  }
}

internal sealed interface TerminationSurveyEvent {
  data class SelectOption(val option: TerminationSurveyOption) : TerminationSurveyEvent
  data object Continue : TerminationSurveyEvent
  data class ChangeFeedbackForReason(
    val option: TerminationSurveyOption,
    val newFeedback: String?,
  ) : TerminationSurveyEvent

  data object Retry : TerminationSurveyEvent

  data object ClearNextStep : TerminationSurveyEvent
}

private sealed interface PartialUiState {
  data object Error : PartialUiState
  data object Loading : PartialUiState
  data class ShowSurvey(
    val nextStep: TerminateInsuranceStep? = null,
    val isNavigationStepLoading: Boolean = false,
    val navigateToSubOptions: Boolean = false,
  ) : PartialUiState
}

internal sealed interface TerminationSurveyState {
  data object ErrorWhileSubmittingReason : TerminationSurveyState
  data object Loading : TerminationSurveyState
  data class ShowSurvey(
    val reasons: List<TerminationReason>,
    val selectedOption: TerminationSurveyOption?,
    val nextStep: TerminateInsuranceStep?,
    val navigateToSubOptions: Boolean,
    val feedbackEmptyWarning: Boolean,
    val isNavigationStepLoading: Boolean,
  ) : TerminationSurveyState
}
