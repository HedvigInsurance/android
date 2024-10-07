package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_PRICE
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.step.survey.ErrorReason.EMPTY_QUOTES
import com.hedvig.android.feature.terminateinsurance.step.survey.ErrorReason.GENERAL
import com.hedvig.android.feature.terminateinsurance.step.survey.SurveyNavigationStep.NavigateToSubOptions
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.ChangeFeedbackForSelectedReason
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.ClearNextStep
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.CloseFullScreenEditText
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.Continue
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.SelectOption
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.ShowFullScreenEditText
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.TryToDowngradePrice
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.TryToUpgradeCoverage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TerminationSurveyViewModel(
  options: List<TerminationSurveyOption>,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  changeTierRepository: ChangeTierRepository,
) : MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
    initialState = TerminationSurveyState(),
    presenter = TerminationSurveyPresenter(
      options,
      terminateInsuranceRepository,
      changeTierRepository,
    ),
  )

internal class TerminationSurveyPresenter(
  private val options: List<TerminationSurveyOption>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val changeTierRepository: ChangeTierRepository,
) : MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(
    lastState: TerminationSurveyState,
  ): TerminationSurveyState {
    var loadBetterQuotesSource by remember { mutableStateOf<ChangeTierCreateSource?>(null) }
    var loadNextStep by remember { mutableStateOf(false) }
    val currentReasonsWithFeedback = remember {
      val initialReasons = (
        lastState.reasons.ifEmpty {
          options.map { option ->
            TerminationReason(option, null)
          }
        }
      ).map {
        it.surveyOption to it.feedBack
      }
      mutableStateMapOf(*initialReasons.toTypedArray())
    }

    var showFullScreenTextField by remember {
      mutableStateOf<TerminationReason?>(null)
    }

    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is ChangeFeedbackForSelectedReason -> {
          showFullScreenTextField = null
          val selectedOption = currentState.selectedOption
          selectedOption?.let { selected ->
            currentReasonsWithFeedback[selected] = event.newFeedback
          }
        }

        is SelectOption -> {
          currentState = currentState.copy(selectedOption = event.option, errorWhileLoadingNextStep = null)
        }

        is Continue -> {
          val selectedOption = currentState.selectedOption ?: return@CollectEvents
          currentState = currentState.copy(errorWhileLoadingNextStep = null)
          if (selectedOption.subOptions.isNotEmpty()) {
            currentState = currentState.copy(nextNavigationStep = NavigateToSubOptions)
          } else {
            loadNextStep = true
          }
        }

        ClearNextStep -> {
          currentState = currentState.copy(nextNavigationStep = null, intentAndIdToRedirectToChangeTierFlow = null)
        }

        is ShowFullScreenEditText -> {
          showFullScreenTextField = TerminationReason(event.option, currentReasonsWithFeedback[event.option])
        }

        CloseFullScreenEditText -> showFullScreenTextField = null

        TryToDowngradePrice -> {
          loadBetterQuotesSource = TERMINATION_BETTER_PRICE
        }

        TryToUpgradeCoverage -> {
          loadBetterQuotesSource = TERMINATION_BETTER_COVERAGE
        }
      }
    }

    LaunchedEffect(loadBetterQuotesSource) {
      val source = loadBetterQuotesSource
      if (source != null) {
        currentState = currentState.copy(actionButtonLoading = true, errorWhileLoadingNextStep = null)
        val insuranceId = terminateInsuranceRepository.getContractId()
        val result =
          changeTierRepository.startChangeTierIntentAndGetQuotesId(insuranceId = insuranceId, source = source)
        result.fold(
          ifLeft = { errorMessage ->
            logcat(LogPriority.ERROR) {
              "Received error while creating changeTierDeductibleIntent from termination flow"
            }
            currentState = currentState.copy(
              actionButtonLoading = false,
              errorWhileLoadingNextStep = GENERAL,
            )
            loadBetterQuotesSource = null
          },
          ifRight = { changeTierIntent ->
            if (changeTierIntent.quotes.isEmpty()) {
              currentState = currentState.copy(
                actionButtonLoading = false,
                errorWhileLoadingNextStep = EMPTY_QUOTES,
              )
              loadBetterQuotesSource = null
            } else {
              currentState = currentState.copy(
                errorWhileLoadingNextStep = null,
                actionButtonLoading = false,
                intentAndIdToRedirectToChangeTierFlow = insuranceId to changeTierIntent,
              )
              loadBetterQuotesSource = null
            }
          },
        )
      }
    }

    LaunchedEffect(loadNextStep) {
      if (loadNextStep) {
        val option = currentState.selectedOption ?: return@LaunchedEffect
        val reasonToSubmit = TerminationReason(option, currentReasonsWithFeedback[option])
        currentState = currentState.copy(navigationStepLoadingForReason = reasonToSubmit)
        currentState = terminateInsuranceRepository
          .submitReasonForCancelling(reasonToSubmit)
          .fold(
            ifLeft = {
              logcat(LogPriority.WARN) { "Received error on submitting reason for termination" }
              loadNextStep = false
              currentState.copy(
                navigationStepLoadingForReason = null,
                errorWhileLoadingNextStep = GENERAL,
              )
            },
            ifRight = { step ->
              logcat(priority = LogPriority.INFO) {
                "Successfully submitted reason for termination: $reasonToSubmit and received next step: $step"
              }
              loadNextStep = false
              currentState.copy(
                navigationStepLoadingForReason = null,
                errorWhileLoadingNextStep = null,
                nextNavigationStep = SurveyNavigationStep.NavigateToNextTerminationStep(step),
              )
            },
          )
      }
    }

    return currentState.copy(
      reasons = currentReasonsWithFeedback.map {
        TerminationReason(it.key, it.value)
      }.sortedBy { it.surveyOption.listIndex },
      showFullScreenEditText = showFullScreenTextField,
    )
  }
}

internal sealed interface TerminationSurveyEvent {
  data class SelectOption(val option: TerminationSurveyOption) : TerminationSurveyEvent

  data object Continue : TerminationSurveyEvent

  data object TryToDowngradePrice : TerminationSurveyEvent

  data object TryToUpgradeCoverage : TerminationSurveyEvent

  data class ShowFullScreenEditText(val option: TerminationSurveyOption) : TerminationSurveyEvent

  data object CloseFullScreenEditText : TerminationSurveyEvent

  data class ChangeFeedbackForSelectedReason(
    val newFeedback: String?,
  ) : TerminationSurveyEvent

  data object ClearNextStep : TerminationSurveyEvent
}

internal data class TerminationSurveyState(
  val reasons: List<TerminationReason> = listOf(),
  val showFullScreenEditText: TerminationReason? = null,
  val selectedOption: TerminationSurveyOption? = null,
  val nextNavigationStep: SurveyNavigationStep? = null,
  // this one is not Boolean entirely for the sake of more convenient testing
  val navigationStepLoadingForReason: TerminationReason? = null,
  val errorWhileLoadingNextStep: ErrorReason? = null,
  val intentAndIdToRedirectToChangeTierFlow: Pair<String, ChangeTierDeductibleIntent>? = null,
  val actionButtonLoading: Boolean = false,
) {
  val continueAllowed: Boolean = selectedOption != null && selectedOption.suggestion == null
}

internal sealed interface SurveyNavigationStep {
  data class NavigateToNextTerminationStep(val step: TerminateInsuranceStep) : SurveyNavigationStep

  data object NavigateToSubOptions : SurveyNavigationStep
}

internal enum class ErrorReason {
  GENERAL,
  EMPTY_QUOTES,
}
