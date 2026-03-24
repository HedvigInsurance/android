package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
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
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: () -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptionsBuilder.() -> Unit) -> Unit,
  closeTerminationFlow: () -> Unit,
  redirectToChangeTierFlow: (Pair<String, IntentOutput>) -> Unit,
) {
  navdestination<TerminateInsuranceDestination.TerminationFailure> {
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = navController::navigateUp,
      navigateBack = navController::popBackStack,
    )
  }
  navdestination<TerminateInsuranceDestination.UnknownScreen> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navController::navigateUp,
      navigateBack = navController::popBackStack,
    )
  }

  navdestination<TerminateInsuranceDestination.TerminationSuccess>(
    TerminateInsuranceDestination.TerminationSuccess,
  ) {
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onDone = {
        if (!navController.popBackStack()) {
          navigateToInsurances {
            typedPopUpTo<TerminateInsuranceDestination.TerminationSuccess> {
              inclusive = true
            }
          }
        }
      },
    )
  }

  navgraph<TerminateInsuranceGraphDestination>(
    startDestination = TerminateInsuranceDestination.StartStep::class,
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.terminateInsurance,
    ),
  ) {
    navdestination<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsuranceGraphDestination = navController
        .getRouteFromBackStack<TerminateInsuranceGraphDestination>(backStackEntry)
      val viewModel: ChooseInsuranceToTerminateViewModel = koinViewModel {
        parametersOf(terminateInsuranceGraphDestination.insuranceId)
      }
      ChooseInsuranceToTerminateDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        closeTerminationFlow = closeTerminationFlow,
        navigateToNextStep = { surveyData, insuranceForCancellation ->
          val commonParams = TerminationGraphParameters(
            insuranceForCancellation.id,
            insuranceForCancellation.displayName,
            insuranceForCancellation.contractExposure,
            insuranceForCancellation.contractGroup,
          )
          navController.navigateToTerminateFlowDestination(
            TerminateInsuranceDestination.TerminationSurveyFirstStep(
              options = surveyData.options,
              action = surveyData.action,
              commonParams = commonParams,
            ),
          )
        },
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationSurveyFirstStep>(
      TerminateInsuranceDestination.TerminationSurveyFirstStep,
    ) {
      val viewModel: TerminationSurveyViewModel = koinViewModel {
        parametersOf(options, action, commonParams.contractId)
      }
      TerminationSurveyDestination(
        viewModel,
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        navigateToSubOptions = { subOptions ->
          navController.navigate(
            TerminateInsuranceDestination.TerminationSurveySecondStep(subOptions, action, commonParams),
          )
        },
        navigateToNextStep = { navStep ->
          navigateFromSurvey(navController, navStep, commonParams)
        },
        navigateToMovingFlow = navigateToMovingFlow,
        openUrl = openUrl,
        redirectToChangeTierFlow = { intent ->
          redirectToChangeTierFlow(intent)
        },
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationSurveySecondStep>(
      TerminateInsuranceDestination.TerminationSurveySecondStep,
    ) {
      val viewModel: TerminationSurveyViewModel = koinViewModel {
        parametersOf(subOptions, action, commonParams.contractId)
      }
      TerminationSurveyDestination(
        viewModel,
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        navigateToSubOptions = null,
        navigateToNextStep = { navStep ->
          navigateFromSurvey(navController, navStep, commonParams)
        },
        navigateToMovingFlow = navigateToMovingFlow,
        openUrl = openUrl,
        redirectToChangeTierFlow = { intent ->
          redirectToChangeTierFlow(intent)
        },
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationDate>(
      TerminateInsuranceDestination.TerminationDate,
    ) {
      val viewModel: TerminationDateViewModel = koinViewModel {
        parametersOf(
          TerminationDateParameters(
            minDate = minDate,
            maxDate = maxDate,
            commonParams,
          ),
        )
      }
      TerminationDateDestination(
        viewModel = viewModel,
        onContinue = { localDate ->
          navController.navigate(
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
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    navdestination<TerminateInsuranceDestination.InsuranceDeletion>(
      TerminateInsuranceDestination.InsuranceDeletion,
    ) {
      InsuranceDeletionDestination(
        displayName = commonParams.insuranceDisplayName,
        exposureName = commonParams.exposureName,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
              extraCoverageItems = extraCoverageItems,
              commonParams = commonParams,
              selectedReasonId = selectedReasonId,
              feedbackComment = feedbackComment,
            ),
          )
        },
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationConfirmation>(
      TerminateInsuranceDestination.TerminationConfirmation,
    ) {
      val viewModel: TerminationConfirmationViewModel = koinViewModel {
        parametersOf(
          terminationType,
          commonParams,
          extraCoverageItems,
          selectedReasonId,
          feedbackComment,
        )
      }
      TerminationConfirmationDestination(
        viewModel = viewModel,
        onContinue = {
          viewModel.emit(TerminationConfirmationEvent.Submit)
        },
        navigateToSuccess = { terminationDate ->
          viewModel.emit(TerminationConfirmationEvent.HandledNavigation)
          navController.navigateToTerminateFlowDestination(
            TerminateInsuranceDestination.TerminationSuccess(terminationDate),
          )
        },
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    navdestination<TerminateInsuranceDestination.DeflectSuggestion>(
      TerminateInsuranceDestination.DeflectSuggestion,
    ) {
      DeflectSuggestionDestination(
        description = description,
        url = url,
        suggestionType = suggestionType,
        navigateUp = navController::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        openUrl = openUrl,
        onContinueTermination = {
          when (val terminationAction = action) {
            is TerminationAction.TerminateWithDate -> {
              navController.navigate(
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
              navController.navigate(
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
}

private fun navigateFromSurvey(
  navController: NavController,
  navStep: SurveyNavigationStep.NavigateToNextTerminationStep,
  commonParams: TerminationGraphParameters,
) {
  val selectedOption = navStep.selectedOption
  val suggestion = selectedOption.suggestion

  // Handle deflection suggestions as full-screen destinations
  if (suggestion != null &&
    (
      suggestion.type == SuggestionType.AUTO_DECOMMISSION || suggestion.type == SuggestionType.AUTO_CANCEL_SOLD ||
        suggestion.type == SuggestionType.AUTO_CANCEL_SCRAPPED
    )
  ) {
    navController.navigate(
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
      navController.navigate(
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
      navController.navigate(
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

private fun <T : Destination> NavController.navigateToTerminateFlowDestination(destination: T) {
  val navOptions: NavOptionsBuilder.() -> Unit = {
    when (destination) {
      is TerminateInsuranceDestination.TerminationSuccess,
      is TerminateInsuranceDestination.TerminationFailure,
      is TerminateInsuranceDestination.UnknownScreen,
      -> {
        typedPopUpTo<TerminateInsuranceGraphDestination> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigate(destination, navOptions)
}
