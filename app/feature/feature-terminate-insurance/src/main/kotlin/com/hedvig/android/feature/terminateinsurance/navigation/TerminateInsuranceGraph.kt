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
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<Destination>.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: () -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: () -> Unit,
  closeTerminationFlow: () -> Unit,
  redirectToChangeTierFlow: (Pair<String, IntentOutput>) -> Unit,
) {
  navdestination<TerminateInsuranceDestination.TerminationFailure> {
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  navdestination<TerminateInsuranceDestination.UnknownScreen> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationSuccess> {
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onDone = {
        if (!navigator.popBackStack()) {
          navigateToInsurances()
        }
      },
    )
  }

  navdestination<TerminateInsuranceGraphDestination> {
    val insuranceId = this.insuranceId
    val viewModel: ChooseInsuranceToTerminateViewModel =
      assistedMetroViewModel<ChooseInsuranceToTerminateViewModel, ChooseInsuranceToTerminateViewModel.Factory> {
        create(insuranceId)
      }
    ChooseInsuranceToTerminateDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      closeTerminationFlow = closeTerminationFlow,
      navigateToNextStep = { surveyData, insuranceForCancellation ->
        val commonParams = TerminationGraphParameters(
          insuranceForCancellation.id,
          insuranceForCancellation.displayName,
          insuranceForCancellation.contractExposure,
          insuranceForCancellation.contractGroup,
        )
        navigator.navigateToTerminateFlowDestination(
          TerminateInsuranceDestination.TerminationSurveyFirstStep(
            options = surveyData.options,
            action = surveyData.action,
            commonParams = commonParams,
          ),
        )
      },
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationSurveyFirstStep> {
    val surveyOptions = options
    val surveyAction = action
    val surveyContractId = commonParams.contractId
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveyOptions, surveyAction, surveyContractId)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToSubOptions = { subOptions ->
        navigator.navigate(
          TerminateInsuranceDestination.TerminationSurveySecondStep(subOptions, action, commonParams),
        )
      },
      navigateToNextStep = { navStep ->
        navigateFromSurvey(navigator, navStep, commonParams)
      },
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationSurveySecondStep> {
    val surveySubOptions = subOptions
    val surveyAction = action
    val surveyContractId = commonParams.contractId
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveySubOptions, surveyAction, surveyContractId)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToSubOptions = { nestedSubOptions ->
        navigator.navigate(
          TerminateInsuranceDestination.TerminationSurveySecondStep(
            nestedSubOptions,
            action,
            commonParams,
          ),
        )
      },
      navigateToNextStep = { navStep ->
        navigateFromSurvey(navigator, navStep, commonParams)
      },
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationDate> {
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
        navigator.navigate(
          TerminateInsuranceDestination.TerminationConfirmation(
            terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination(
              localDate,
            ),
            extraCoverageItems = extraCoverageItems,
            commonParams = commonParams,
            selectedReasonId = selectedReasonId,
            feedbackComment = feedbackComment,
          ),
        )
      },
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<TerminateInsuranceDestination.InsuranceDeletion> {
    InsuranceDeletionDestination(
      displayName = commonParams.insuranceDisplayName,
      exposureName = commonParams.exposureName,
      onContinue = {
        navigator.navigate(
          TerminateInsuranceDestination.TerminationConfirmation(
            terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
            extraCoverageItems = extraCoverageItems,
            commonParams = commonParams,
            selectedReasonId = selectedReasonId,
            feedbackComment = feedbackComment,
          ),
        )
      },
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationConfirmation> {
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
        navigator.navigateToTerminateFlowDestination(
          TerminateInsuranceDestination.TerminationSuccess(terminationDate),
        )
      },
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  navdestination<TerminateInsuranceDestination.DeflectSuggestion> {
    DeflectSuggestionDestination(
      description = description,
      suggestionType = suggestionType,
      navigateUp = navigator::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      onContinueTermination = {
        when (val terminationAction = action) {
          is TerminationAction.TerminateWithDate -> {
            navigator.navigate(
              TerminateInsuranceDestination.TerminationDate(
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
            navigator.navigate(
              TerminateInsuranceDestination.InsuranceDeletion(
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
  navigator: Navigator,
  navStep: SurveyNavigationStep.NavigateToNextTerminationStep,
  commonParams: TerminationGraphParameters,
) {
  val selectedOption = navStep.selectedOption
  val suggestion = selectedOption.suggestion

  // Handle deflection suggestions as full-screen destinations
  if (suggestion != null &&
    suggestion.type in SuggestionType.DEFLECT_TYPES
  ) {
    navigator.navigate(
      TerminateInsuranceDestination.DeflectSuggestion(
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
      navigator.navigate(
        TerminateInsuranceDestination.TerminationDate(
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
      navigator.navigate(
        TerminateInsuranceDestination.InsuranceDeletion(
          commonParams = commonParams,
          extraCoverageItems = terminationAction.extraCoverageItems,
          selectedReasonId = selectedOption.id,
          feedbackComment = navStep.feedbackText,
        ),
      )
    }
  }
}

private fun Navigator.navigateToTerminateFlowDestination(destination: Destination) {
  when (destination) {
    is TerminateInsuranceDestination.TerminationSuccess,
    is TerminateInsuranceDestination.TerminationFailure,
    is TerminateInsuranceDestination.UnknownScreen,
    -> navigate<TerminateInsuranceGraphDestination>(destination, inclusive = true)

    else -> navigate(destination)
  }
}
