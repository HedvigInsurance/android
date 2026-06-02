package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deflect.DeflectSuggestionDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.survey.SurveyNavigationStep
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyDestination
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationEvent
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  backStack: MutableList<HedvigNavKey>,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: () -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: () -> Unit,
  closeTerminationFlow: () -> Unit,
  redirectToChangeTierFlow: (Pair<String, IntentOutput>) -> Unit,
) {
  navdestination<TerminationFailureKey> {
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
    )
  }
  navdestination<UnknownScreenKey> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
    )
  }

  navdestination<TerminationSuccessKey> {
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onDone = {
        if (!backStack.popBackStack()) {
          navigateToInsurances()
        }
      },
    )
  }

  navdestination<TerminateInsuranceKey> {
    val insuranceId = this.insuranceId
    val viewModel: ChooseInsuranceToTerminateViewModel =
      assistedMetroViewModel<ChooseInsuranceToTerminateViewModel, ChooseInsuranceToTerminateViewModel.Factory> {
        create(insuranceId)
      }
    ChooseInsuranceToTerminateDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      closeTerminationFlow = closeTerminationFlow,
      navigateToNextStep = { surveyData, insuranceForCancellation ->
        val commonParams = TerminationGraphParameters(
          insuranceForCancellation.id,
          insuranceForCancellation.displayName,
          insuranceForCancellation.contractExposure,
          insuranceForCancellation.contractGroup,
        )
        backStack.navigateToTerminateFlowDestination(
          TerminationSurveyFirstStepKey(
            options = surveyData.options,
            action = surveyData.action,
            commonParams = commonParams,
          ),
        )
      },
    )
  }

  navdestination<TerminationSurveyFirstStepKey> {
    val surveyOptions = options
    val surveyAction = action
    val surveyContractId = commonParams.contractId
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveyOptions, surveyAction, surveyContractId)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToSubOptions = { subOptions ->
        backStack.add(
          TerminationSurveySecondStepKey(subOptions, action, commonParams),
        )
      },
      navigateToNextStep = { navStep ->
        navigateFromSurvey(backStack, navStep, commonParams)
      },
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  navdestination<TerminationSurveySecondStepKey> {
    val surveySubOptions = subOptions
    val surveyAction = action
    val surveyContractId = commonParams.contractId
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveySubOptions, surveyAction, surveyContractId)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToSubOptions = { nestedSubOptions ->
        backStack.add(
          TerminationSurveySecondStepKey(
            nestedSubOptions,
            action,
            commonParams,
          ),
        )
      },
      navigateToNextStep = { navStep ->
        navigateFromSurvey(backStack, navStep, commonParams)
      },
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  navdestination<TerminationDateKey> {
    val terminationDateParameters = TerminationDateParameters(
      minDate = minDate,
      maxDate = maxDate,
      commonParams,
    )
    val viewModel: TerminationDateViewModel =
      assistedMetroViewModel<TerminationDateViewModel, TerminationDateViewModel.Factory> {
        create(terminationDateParameters)
      }
    TerminationDateDestination(
      viewModel = viewModel,
      onContinue = { localDate ->
        backStack.add(
          TerminationConfirmationKey(
            terminationType = TerminationConfirmationKey.TerminationType.Termination(
              localDate,
            ),
            extraCoverageItems = extraCoverageItems,
            commonParams = commonParams,
            selectedReasonId = selectedReasonId,
            feedbackComment = feedbackComment,
          ),
        )
      },
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<InsuranceDeletionKey> {
    InsuranceDeletionDestination(
      displayName = commonParams.insuranceDisplayName,
      exposureName = commonParams.exposureName,
      onContinue = {
        backStack.add(
          TerminationConfirmationKey(
            terminationType = TerminationConfirmationKey.TerminationType.Deletion,
            extraCoverageItems = extraCoverageItems,
            commonParams = commonParams,
            selectedReasonId = selectedReasonId,
            feedbackComment = feedbackComment,
          ),
        )
      },
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<TerminationConfirmationKey> {
    val confirmationTerminationType = terminationType
    val confirmationInsuranceInfo = commonParams
    val confirmationExtraCoverageItems = extraCoverageItems
    val confirmationSelectedReasonId = selectedReasonId
    val confirmationFeedbackComment = feedbackComment
    val viewModel: TerminationConfirmationViewModel =
      assistedMetroViewModel<TerminationConfirmationViewModel, TerminationConfirmationViewModel.Factory> {
        create(
          confirmationTerminationType,
          confirmationInsuranceInfo,
          confirmationExtraCoverageItems,
          confirmationSelectedReasonId,
          confirmationFeedbackComment,
        )
      }
    TerminationConfirmationDestination(
      viewModel = viewModel,
      onContinue = {
        viewModel.emit(TerminationConfirmationEvent.Submit)
      },
      navigateToSuccess = { terminationDate ->
        viewModel.emit(TerminationConfirmationEvent.HandledNavigation)
        backStack.navigateToTerminateFlowDestination(
          TerminationSuccessKey(terminationDate),
        )
      },
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<DeflectSuggestionKey> {
    DeflectSuggestionDestination(
      description = description,
      suggestionType = suggestionType,
      navigateUp = backStack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      onContinueTermination = {
        when (val terminationAction = action) {
          is TerminationAction.TerminateWithDate -> {
            backStack.add(
              TerminationDateKey(
                minDate = terminationAction.minDate,
                maxDate = terminationAction.maxDate,
                extraCoverageItems = terminationAction.extraCoverageItems,
                commonParams = commonParams,
                selectedReasonId = selectedReasonId,
                feedbackComment = feedbackComment,
              ),
            )
          }

          is TerminationAction.DeleteInsurance -> {
            backStack.add(
              InsuranceDeletionKey(
                commonParams = commonParams,
                extraCoverageItems = terminationAction.extraCoverageItems,
                selectedReasonId = selectedReasonId,
                feedbackComment = feedbackComment,
              ),
            )
          }
        }
      },
    )
  }
}

private fun navigateFromSurvey(
  backStack: MutableList<HedvigNavKey>,
  navStep: SurveyNavigationStep.NavigateToNextTerminationStep,
  commonParams: TerminationGraphParameters,
) {
  val selectedOption = navStep.selectedOption
  val suggestion = selectedOption.suggestion

  // Handle deflection suggestions as full-screen destinations
  if (suggestion != null &&
    suggestion.type in SuggestionType.DEFLECT_TYPES
  ) {
    backStack.add(
      DeflectSuggestionKey(
        description = suggestion.description,
        url = suggestion.url,
        suggestionType = suggestion.type,
        commonParams = commonParams,
        action = navStep.action,
        selectedReasonId = selectedOption.id,
        feedbackComment = navStep.feedbackText,
      ),
    )
    return
  }

  // Navigate based on the termination action
  when (val terminationAction = navStep.action) {
    is TerminationAction.TerminateWithDate -> {
      backStack.add(
        TerminationDateKey(
          minDate = terminationAction.minDate,
          maxDate = terminationAction.maxDate,
          extraCoverageItems = terminationAction.extraCoverageItems,
          commonParams = commonParams,
          selectedReasonId = selectedOption.id,
          feedbackComment = navStep.feedbackText,
        ),
      )
    }

    is TerminationAction.DeleteInsurance -> {
      backStack.add(
        InsuranceDeletionKey(
          commonParams = commonParams,
          extraCoverageItems = terminationAction.extraCoverageItems,
          selectedReasonId = selectedOption.id,
          feedbackComment = navStep.feedbackText,
        ),
      )
    }
  }
}

private fun MutableList<HedvigNavKey>.navigateToTerminateFlowDestination(destination: HedvigNavKey) {
  when (destination) {
    is TerminationSuccessKey,
    is TerminationFailureKey,
    is UnknownScreenKey,
    -> navigateAndPopUpTo<TerminateInsuranceKey>(destination, inclusive = true)

    else -> add(destination)
  }
}
