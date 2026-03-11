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
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.terminateinsurance.data.CarDecomEligibility
import com.hedvig.android.feature.terminateinsurance.data.CarDeflectionRoute
import com.hedvig.android.feature.terminateinsurance.data.CarDeflectionRouter
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationFlowComputations
import com.hedvig.android.feature.terminateinsurance.data.TerminationInfo
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
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

internal class TerminationSurveyViewModel(
  options: List<TerminationSurveyOption>,
  terminateInsuranceRepository: TerminateInsuranceRepository,
  changeTierRepository: ChangeTierRepository,
  terminationInfo: TerminationInfo? = null,
) : MoleculeViewModel<TerminationSurveyEvent, TerminationSurveyState>(
    initialState = TerminationSurveyState(options),
    presenter = TerminationSurveyPresenter(
      options,
      terminateInsuranceRepository,
      changeTierRepository,
      terminationInfo,
    ),
  )

internal class TerminationSurveyPresenter(
  private val options: List<TerminationSurveyOption>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  private val changeTierRepository: ChangeTierRepository,
  private val terminationInfo: TerminationInfo? = null,
) : MoleculePresenter<TerminationSurveyEvent, TerminationSurveyState> {
  @Composable
  override fun MoleculePresenterScope<TerminationSurveyEvent>.present(
    lastState: TerminationSurveyState,
  ): TerminationSurveyState {
    var loadBetterQuotesSource by remember { mutableStateOf<ChangeTierCreateSource?>(null) }
    var loadNextStep by remember { mutableStateOf(false) }
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
            currentState = currentState.copy(nextNavigationStep = NavigateToSubOptions)
          } else {
            loadNextStep = true
          }
        }

        ClearNextStep -> {
          currentState = currentState.copy(nextNavigationStep = null, intentAndIdToRedirectToChangeTierFlow = null)
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
          val deflect = changeTierIntent.deflectOutput
          if (deflect != null) {
            Snapshot.withMutableSnapshot {
              val optionsToDisable = options.filter { option ->
                option.suggestion is SurveyOptionSuggestion.Known.Action.DowngradePriceByChangingTier ||
                  option.suggestion is SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier
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
                  option.suggestion is SurveyOptionSuggestion.Known.Action.DowngradePriceByChangingTier ||
                    option.suggestion is SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier
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
                intentAndIdToRedirectToChangeTierFlow = insuranceId to intent,
              )
              loadBetterQuotesSource = null
            }
          }
        },
      )
    }

    if (loadNextStep) {
      LaunchedEffect(Unit) {
        val selectedOption = currentState.selectedOption ?: return@LaunchedEffect
        currentState = currentState.copy(navigationStepLoading = true)
        val info = terminationInfo
        if (info != null) {
          val deflection = CarDeflectionRouter.route(
            typeOfContract = info.typeOfContract,
            selectedOptionId = selectedOption.id,
            decomEligible = CarDecomEligibility.isEligible(info.typeOfContract, info.commencementDate),
          )
          val nextStep: TerminateInsuranceStep = when (deflection) {
            CarDeflectionRoute.AutoCancel -> {
              TerminateInsuranceStep.DeflectAutoCancelStep(
                title = "Your insurance will be cancelled automatically",
                message = when (selectedOption.id) {
                  "CAR_SOLD" -> "When the new owner registers the car, your insurance will be cancelled automatically."
                  "CAR_SCRAPPED" -> "When the car is scrapped, your insurance will be cancelled automatically."
                  else -> "When the car is decommissioned, your insurance will be cancelled automatically."
                },
                extraMessage = "You don't need to do anything. We'll handle it.",
              )
            }

            CarDeflectionRoute.AutoDecommission -> {
              TerminateInsuranceStep.DeflectAutoDecommissionStep(
                title = if (selectedOption.id == "CAR_RECOMMISSIONED") {
                  "Your car has been recommissioned"
                } else {
                  "Your car is decommissioned"
                },
                message = if (selectedOption.id == "CAR_RECOMMISSIONED") {
                  "Your insurance will be automatically updated to match your car's new status."
                } else {
                  "Your insurance will be automatically adjusted while your car is decommissioned."
                },
                info = if (selectedOption.id == "CAR_RECOMMISSIONED") {
                  null
                } else {
                  "You'll be notified when the changes take effect."
                },
                explanations = if (selectedOption.id == "CAR_RECOMMISSIONED") {
                  emptyList()
                } else {
                  listOf(
                    "What's covered" to "Your car is still covered against theft and damage while decommissioned.",
                    "What it costs" to "You'll pay a reduced premium while your car is decommissioned.",
                  )
                },
              )
            }

            null -> {
              val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
              if (TerminationFlowComputations.shouldDelete(info.masterInceptionDate, today)) {
                TerminateInsuranceStep.InsuranceDeletion(info.existingAddons)
              } else {
                val minDate = TerminationFlowComputations.minDate(info.masterInceptionDate, today)
                TerminateInsuranceStep.TerminateInsuranceDate(
                  minDate = minDate,
                  maxDate = TerminationFlowComputations.maxDate(minDate),
                  extraCoverageItems = info.existingAddons,
                )
              }
            }
          }
          loadNextStep = false
          currentState = currentState.copy(
            navigationStepLoading = false,
            nextNavigationStep = SurveyNavigationStep.NavigateToNextTerminationStep(nextStep),
          )
        } else {
          // Fallback to old server-driven flow
          currentState = terminateInsuranceRepository
            .submitReasonForCancelling(selectedOption, feedbackText)
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
  val nextNavigationStep: SurveyNavigationStep?,
  val navigationStepLoading: Boolean,
  val errorWhileLoadingNextStep: Boolean,
  val showEmptyQuotesDialog: DeflectType? = null,
  val intentAndIdToRedirectToChangeTierFlow: Pair<String, IntentOutput>?,
  val actionButtonLoading: Boolean,
) {
  val selectedOption: TerminationSurveyOption? = reasons.firstOrNull { it.id == selectedOptionId }
  val continueAllowed: Boolean = selectedOption != null &&
    (selectedOption.suggestion == null || selectedOption.suggestion !is SurveyOptionSuggestion.Known.Action)

  constructor(reasons: List<TerminationSurveyOption>) : this(
    reasons = reasons,
    feedbackText = null,
    showFullScreenEditText = false,
    selectedOptionId = null,
    nextNavigationStep = null,
    navigationStepLoading = false,
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

internal sealed interface SurveyNavigationStep {
  data class NavigateToNextTerminationStep(val step: TerminateInsuranceStep) : SurveyNavigationStep

  data object NavigateToSubOptions : SurveyNavigationStep
}
