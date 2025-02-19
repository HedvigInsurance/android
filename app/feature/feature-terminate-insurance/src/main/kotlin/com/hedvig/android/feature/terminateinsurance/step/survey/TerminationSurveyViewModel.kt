package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_PRICE
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.step.survey.SurveyNavigationStep.NavigateToSubOptions
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.ClearEmptyQuotesDialog
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.ClearNextStep
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.CloseFullScreenEditText
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.Continue
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyEvent.EditTextFeedback
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
    initialState = TerminationSurveyState.Empty,
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
    var feedbackText: String? by remember { mutableStateOf(null) }

    var showFullScreenTextField by remember {
      mutableStateOf(false)
    }
    var disabledOptionsIdsDueToEmptyResultingQuotes by remember { mutableStateOf<List<String>>(emptyList()) }

    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is EditTextFeedback -> {
          showFullScreenTextField = false
          feedbackText = event.newFeedback
        }

        is SelectOption -> {
          currentState = currentState.copy(selectedOptionId = event.option.id, errorWhileLoadingNextStep = false)
        }

        is Continue -> {
          val selectedOption = currentState.selectedOption ?: return@CollectEvents
          currentState = currentState.copy(errorWhileLoadingNextStep = false)
          if (selectedOption.subOptions.isNotEmpty()) {
            currentState = currentState.copy(nextNavigationStep = NavigateToSubOptions)
          } else {
            loadNextStep = true
          }
        }

        ClearNextStep -> {
          currentState = currentState.copy(nextNavigationStep = null, intentAndIdToRedirectToChangeTierFlow = null)
        }

        is ShowFullScreenEditText -> showFullScreenTextField = true
        CloseFullScreenEditText -> showFullScreenTextField = false
        TryToDowngradePrice -> loadBetterQuotesSource = TERMINATION_BETTER_PRICE
        TryToUpgradeCoverage -> loadBetterQuotesSource = TERMINATION_BETTER_COVERAGE
        ClearEmptyQuotesDialog -> {
          currentState = currentState.copy(showEmptyQuotesDialog = false)
        }
      }
    }

    LaunchedEffect(loadBetterQuotesSource) {
      val source = loadBetterQuotesSource ?: return@LaunchedEffect
      currentState = currentState.copy(actionButtonLoading = true, errorWhileLoadingNextStep = false)
      val insuranceId = terminateInsuranceRepository.getContractId()
      val result =
        changeTierRepository.startChangeTierIntentAndGetQuotesId(insuranceId = insuranceId, source = source)
      result.fold(
        ifLeft = { errorMessage ->
          logcat(LogPriority.ERROR) {
            "Received error while creating changeTierDeductibleIntent from termination flow : $errorMessage"
          }
          currentState = currentState.copy(
            actionButtonLoading = false,
            errorWhileLoadingNextStep = true,
          )
          loadBetterQuotesSource = null
        },
        ifRight = { changeTierIntent ->
          if (changeTierIntent.quotes.isEmpty()) {
            Snapshot.withMutableSnapshot {
              val optionsToDisable = options.filter { option ->
                option.suggestion is SurveyOptionSuggestion.Action.DowngradePriceByChangingTier ||
                  option.suggestion is SurveyOptionSuggestion.Action.UpgradeCoverageByChangingTier
              }
              disabledOptionsIdsDueToEmptyResultingQuotes = optionsToDisable.map { it.id }
              currentState = currentState.copy(
                actionButtonLoading = false,
                errorWhileLoadingNextStep = false,
                showEmptyQuotesDialog = true,
                selectedOptionId = null,
              )
              loadBetterQuotesSource = null
            }
          } else {
            currentState = currentState.copy(
              errorWhileLoadingNextStep = false,
              actionButtonLoading = false,
              intentAndIdToRedirectToChangeTierFlow = insuranceId to changeTierIntent,
            )
            loadBetterQuotesSource = null
          }
        },
      )
    }

    if (loadNextStep) {
      LaunchedEffect(Unit) {
        val reasonToSubmit = currentState.selectedOption ?: return@LaunchedEffect
        currentState = currentState.copy(navigationStepLoading = true)
        currentState = terminateInsuranceRepository
          .submitReasonForCancelling(reasonToSubmit, feedbackText)
          .fold(
            ifLeft = {
              logcat(LogPriority.WARN) { "Received error on submitting reason for termination" }
              loadNextStep = false
              currentState.copy(
                navigationStepLoading = false,
                errorWhileLoadingNextStep = true,
              )
            },
            ifRight = { step ->
              logcat(priority = LogPriority.INFO) {
                "Successfully submitted reason for termination: $reasonToSubmit and received next step: $step"
              }
              loadNextStep = false
              currentState.copy(
                navigationStepLoading = false,
                errorWhileLoadingNextStep = false,
                nextNavigationStep = SurveyNavigationStep.NavigateToNextTerminationStep(step),
              )
            },
          )
      }
    }

    return currentState.copy(
      reasons = options.map { option ->
        if (option.id in disabledOptionsIdsDueToEmptyResultingQuotes) {
          option.copy(isDisabled = true)
        } else {
          option
        }
      },
      showFullScreenEditText = showFullScreenTextField,
    )
  }
}

internal sealed interface TerminationSurveyEvent {
  data class SelectOption(val option: TerminationSurveyOption) : TerminationSurveyEvent

  data object Continue : TerminationSurveyEvent

  data object TryToDowngradePrice : TerminationSurveyEvent

  data object TryToUpgradeCoverage : TerminationSurveyEvent

  data object ShowFullScreenEditText : TerminationSurveyEvent

  data object CloseFullScreenEditText : TerminationSurveyEvent

  data class EditTextFeedback(val newFeedback: String?) : TerminationSurveyEvent

  data object ClearNextStep : TerminationSurveyEvent

  data object ClearEmptyQuotesDialog : TerminationSurveyEvent
}

internal data class TerminationSurveyState(
  val reasons: List<TerminationSurveyOption>,
  val feedbackText: String,
  val showFullScreenEditText: Boolean,
  val selectedOptionId: String?,
  val nextNavigationStep: SurveyNavigationStep?,
  val navigationStepLoading: Boolean,
  val errorWhileLoadingNextStep: Boolean,
  val showEmptyQuotesDialog: Boolean,
  val intentAndIdToRedirectToChangeTierFlow: Pair<String, ChangeTierDeductibleIntent>?,
  val actionButtonLoading: Boolean,
) {
  val selectedOption: TerminationSurveyOption? = reasons.firstOrNull { it.id == selectedOptionId }
  val continueAllowed: Boolean = selectedOption != null && selectedOption.suggestion == null

  companion object {
    val Empty = TerminationSurveyState(
      reasons = emptyList(),
      feedbackText = "",
      showFullScreenEditText = false,
      selectedOptionId = null,
      nextNavigationStep = null,
      navigationStepLoading = false,
      errorWhileLoadingNextStep = false,
      showEmptyQuotesDialog = false,
      intentAndIdToRedirectToChangeTierFlow = null,
      actionButtonLoading = false,
    )
  }
}

internal sealed interface SurveyNavigationStep {
  data class NavigateToNextTerminationStep(val step: TerminateInsuranceStep) : SurveyNavigationStep

  data object NavigateToSubOptions : SurveyNavigationStep
}
