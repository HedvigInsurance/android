package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_PRICE
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.navigation.DeflectSuggestionKey
import com.hedvig.android.feature.terminateinsurance.navigation.InsuranceDeletionKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationSurveySecondStepKey
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
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

internal class TerminationSurveyViewModel @AssistedInject constructor(
  @Assisted options: List<TerminationSurveyOption>,
  @Assisted action: TerminationAction,
  @Assisted commonParams: TerminationGraphParameters,
  changeTierRepository: ChangeTierRepository,
  backstack: Backstack,
) : MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
    initialState = TerminationSurveyState(options),
    presenter = TerminationSurveyPresenter(
      options,
      action,
      commonParams,
      changeTierRepository,
      backstack,
    ),
  ) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(ActivityRetainedScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted options: List<TerminationSurveyOption>,
      @Assisted action: TerminationAction,
      @Assisted commonParams: TerminationGraphParameters,
    ): TerminationSurveyViewModel
  }
}

internal class TerminationSurveyPresenter(
  private val options: List<TerminationSurveyOption>,
  private val action: TerminationAction,
  private val commonParams: TerminationGraphParameters,
  private val changeTierRepository: ChangeTierRepository,
  private val backstack: Backstack,
) : MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  private val contractId: String = commonParams.contractId

  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(
    lastState: TerminationSurveyState,
  ): TerminationSurveyState {
    var loadBetterQuotesSource by remember { mutableStateOf<ChangeTierCreateSource?>(null) }
    var feedbackText: String? by remember { mutableStateOf(lastState.feedbackText) }

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
            backstack.add(
              TerminationSurveySecondStepKey(selectedOption.subOptions, action, commonParams),
            )
          } else {
            backstack.navigateAfterSurvey(selectedOption, feedbackText, action, commonParams)
          }
        }

        ClearNextStep -> {
          currentState = currentState.copy(intentAndIdToRedirectToChangeTierFlow = null)
        }

        is ShowFullScreenEditText -> {
          showFullScreenTextField = true
        }

        CloseFullScreenEditText -> {
          showFullScreenTextField = false
        }

        TryToDowngradePrice -> {
          loadBetterQuotesSource = TERMINATION_BETTER_PRICE
        }

        TryToUpgradeCoverage -> {
          loadBetterQuotesSource = TERMINATION_BETTER_COVERAGE
        }

        ClearEmptyQuotesDialog -> {
          currentState = currentState.copy(showEmptyQuotesDialog = null)
        }
      }
    }

    LaunchedEffect(loadBetterQuotesSource) {
      val source = loadBetterQuotesSource ?: return@LaunchedEffect
      currentState = currentState.copy(actionButtonLoading = true, errorWhileLoadingNextStep = false)
      val result =
        changeTierRepository.startChangeTierIntentAndGetQuotesId(insuranceId = contractId, source = source)
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
          val deflect = changeTierIntent.deflectOutput
          if (deflect != null) {
            Snapshot.withMutableSnapshot {
              val optionsToDisable = options.filter { option ->
                option.suggestion?.type == SuggestionType.DOWNGRADE_PRICE ||
                  option.suggestion?.type == SuggestionType.UPGRADE_COVERAGE
              }
              disabledOptionsIdsDueToEmptyResultingQuotes = optionsToDisable.map { it.id }
              currentState = currentState.copy(
                actionButtonLoading = false,
                errorWhileLoadingNextStep = false,
                showEmptyQuotesDialog = DeflectType.Deflect(
                  deflect.title,
                  deflect.message,
                ),
                selectedOptionId = null,
              )
              loadBetterQuotesSource = null
              return@LaunchedEffect
            }
          }
          val intent = changeTierIntent.intentOutput
          if (intent != null) {
            if (intent.quotes.isEmpty()) {
              Snapshot.withMutableSnapshot {
                val optionsToDisable = options.filter { option ->
                  option.suggestion?.type == SuggestionType.DOWNGRADE_PRICE ||
                    option.suggestion?.type == SuggestionType.UPGRADE_COVERAGE
                }
                disabledOptionsIdsDueToEmptyResultingQuotes = optionsToDisable.map { it.id }
                currentState = currentState.copy(
                  actionButtonLoading = false,
                  errorWhileLoadingNextStep = false,
                  showEmptyQuotesDialog = DeflectType.EmptyQuotes,
                  selectedOptionId = null,
                )
                loadBetterQuotesSource = null
              }
            } else {
              currentState = currentState.copy(
                errorWhileLoadingNextStep = false,
                actionButtonLoading = false,
                intentAndIdToRedirectToChangeTierFlow = contractId to intent,
              )
              loadBetterQuotesSource = null
            }
          }
        },
      )
    }

    return currentState.copy(
      reasons = options.map { option ->
        if (option.id in disabledOptionsIdsDueToEmptyResultingQuotes) {
          option.copy(isDisabled = true)
        } else {
          option
        }
      },
      feedbackText = feedbackText,
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
  val feedbackText: String?,
  val showFullScreenEditText: Boolean,
  val selectedOptionId: String?,
  val errorWhileLoadingNextStep: Boolean,
  val showEmptyQuotesDialog: DeflectType? = null,
  val intentAndIdToRedirectToChangeTierFlow: Pair<String, IntentOutput>?,
  val actionButtonLoading: Boolean,
) {
  val selectedOption: TerminationSurveyOption? = reasons.firstOrNull { it.id == selectedOptionId }
  val continueAllowed: Boolean = selectedOption != null &&
    selectedOption.suggestion?.type !in setOf(
      SuggestionType.UPDATE_ADDRESS,
      SuggestionType.UPGRADE_COVERAGE,
      SuggestionType.DOWNGRADE_PRICE,
      SuggestionType.REDIRECT,
    )

  constructor(reasons: List<TerminationSurveyOption>) : this(
    reasons = reasons,
    feedbackText = null,
    showFullScreenEditText = false,
    selectedOptionId = null,
    errorWhileLoadingNextStep = false,
    showEmptyQuotesDialog = null,
    intentAndIdToRedirectToChangeTierFlow = null,
    actionButtonLoading = false,
  )
}

internal sealed interface DeflectType {
  data object EmptyQuotes : DeflectType

  data class Deflect(
    val title: String,
    val message: String,
  ) : DeflectType
}

private fun Backstack.navigateAfterSurvey(
  selectedOption: TerminationSurveyOption,
  feedbackText: String?,
  action: TerminationAction,
  commonParams: TerminationGraphParameters,
) {
  val suggestion = selectedOption.suggestion
  if (suggestion != null && suggestion.type in SuggestionType.DEFLECT_TYPES) {
    add(
      DeflectSuggestionKey(
        description = suggestion.description,
        url = suggestion.url,
        suggestionType = suggestion.type,
        commonParams = commonParams,
        action = action,
        selectedReasonId = selectedOption.id,
        feedbackComment = feedbackText,
      ),
    )
    return
  }
  when (val terminationAction = action) {
    is TerminationAction.TerminateWithDate -> add(
      TerminationDateKey(
        minDate = terminationAction.minDate,
        maxDate = terminationAction.maxDate,
        extraCoverageItems = terminationAction.extraCoverageItems,
        commonParams = commonParams,
        selectedReasonId = selectedOption.id,
        feedbackComment = feedbackText,
      ),
    )

    is TerminationAction.DeleteInsurance -> add(
      InsuranceDeletionKey(
        commonParams = commonParams,
        extraCoverageItems = terminationAction.extraCoverageItems,
        selectedReasonId = selectedOption.id,
        feedbackComment = feedbackText,
      ),
    )
  }
}
