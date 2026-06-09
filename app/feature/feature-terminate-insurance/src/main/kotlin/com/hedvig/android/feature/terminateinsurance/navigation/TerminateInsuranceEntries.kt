package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deflect.DeflectSuggestionDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
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
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.terminateInsuranceEntries(
  windowSizeClass: WindowSizeClass,
  backstack: Backstack,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: () -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: () -> Unit,
  closeTerminationFlow: () -> Unit,
  redirectToChangeTierFlow: (Pair<String, IntentOutput>) -> Unit,
) {
  entry<TerminationFailureKey> { key ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(key.message),
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }
  entry<UnknownScreenKey> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }

  entry<TerminationSuccessKey> { key ->
    TerminationSuccessDestination(
      terminationDate = key.terminationDate,
      onDone = {
        // Reached alone via deep link → land on the Insurances tab rather than exiting the app
        // (popBackstack would finish the Activity at the root).
        if (backstack.entries.size > 1) {
          backstack.popBackstack()
        } else {
          navigateToInsurances()
        }
      },
    )
  }

  entry<TerminateInsuranceKey> { key ->
    val insuranceId = key.insuranceId
    val viewModel: ChooseInsuranceToTerminateViewModel =
      assistedMetroViewModel<ChooseInsuranceToTerminateViewModel, ChooseInsuranceToTerminateViewModel.Factory> {
        create(insuranceId)
      }
    ChooseInsuranceToTerminateDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  entry<TerminationSurveyFirstStepKey> { key ->
    val surveyOptions = key.options
    val surveyAction = key.action
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveyOptions, surveyAction, key.commonParams)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  entry<TerminationSurveySecondStepKey> { key ->
    val surveySubOptions = key.subOptions
    val surveyAction = key.action
    val viewModel: TerminationSurveyViewModel =
      assistedMetroViewModel<TerminationSurveyViewModel, TerminationSurveyViewModel.Factory> {
        create(surveySubOptions, surveyAction, key.commonParams)
      }
    TerminationSurveyDestination(
      viewModel,
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      navigateToMovingFlow = navigateToMovingFlow,
      openUrl = openUrl,
      redirectToChangeTierFlow = { intent ->
        redirectToChangeTierFlow(intent)
      },
    )
  }

  entry<TerminationDateKey> { key ->
    val terminationDateParameters = TerminationDateParameters(
      minDate = key.minDate,
      maxDate = key.maxDate,
      key.commonParams,
    )
    val viewModel: TerminationDateViewModel =
      assistedMetroViewModel<TerminationDateViewModel, TerminationDateViewModel.Factory> {
        create(terminationDateParameters)
      }
    TerminationDateDestination(
      viewModel = viewModel,
      onContinue = { localDate ->
        backstack.add(
          TerminationConfirmationKey(
            terminationType = TerminationConfirmationKey.TerminationType.Termination(
              localDate,
            ),
            extraCoverageItems = key.extraCoverageItems,
            commonParams = key.commonParams,
            selectedReasonId = key.selectedReasonId,
            feedbackComment = key.feedbackComment,
          ),
        )
      },
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  entry<InsuranceDeletionKey> { key ->
    InsuranceDeletionDestination(
      displayName = key.commonParams.insuranceDisplayName,
      exposureName = key.commonParams.exposureName,
      onContinue = {
        backstack.add(
          TerminationConfirmationKey(
            terminationType = TerminationConfirmationKey.TerminationType.Deletion,
            extraCoverageItems = key.extraCoverageItems,
            commonParams = key.commonParams,
            selectedReasonId = key.selectedReasonId,
            feedbackComment = key.feedbackComment,
          ),
        )
      },
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  entry<TerminationConfirmationKey> { key ->
    val confirmationTerminationType = key.terminationType
    val confirmationInsuranceInfo = key.commonParams
    val confirmationExtraCoverageItems = key.extraCoverageItems
    val confirmationSelectedReasonId = key.selectedReasonId
    val confirmationFeedbackComment = key.feedbackComment
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
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
    )
  }

  entry<DeflectSuggestionKey> { key ->
    DeflectSuggestionDestination(
      description = key.description,
      suggestionType = key.suggestionType,
      navigateUp = backstack::navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      onContinueTermination = {
        when (val terminationAction = key.action) {
          is TerminationAction.TerminateWithDate -> {
            backstack.add(
              TerminationDateKey(
                minDate = terminationAction.minDate,
                maxDate = terminationAction.maxDate,
                extraCoverageItems = terminationAction.extraCoverageItems,
                commonParams = key.commonParams,
                selectedReasonId = key.selectedReasonId,
                feedbackComment = key.feedbackComment,
              ),
            )
          }

          is TerminationAction.DeleteInsurance -> {
            backstack.add(
              InsuranceDeletionKey(
                commonParams = key.commonParams,
                extraCoverageItems = terminationAction.extraCoverageItems,
                selectedReasonId = key.selectedReasonId,
                feedbackComment = key.feedbackComment,
              ),
            )
          }
        }
      },
    )
  }
}
