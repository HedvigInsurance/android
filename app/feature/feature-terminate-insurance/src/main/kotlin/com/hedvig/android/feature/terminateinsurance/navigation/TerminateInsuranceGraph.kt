package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyDestination
import com.hedvig.android.feature.terminateinsurance.step.survey.TerminationSurveyViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptions) -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  composable<TerminateInsuranceDestination.TerminationSuccess> { backStackEntry ->
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onDone = {
        if (!navController.popBackStack()) {
          // In the deep link situation, we want to navigate to Insurances when we're successfully done with this flow
          navigateToInsurances(
            navOptions {
              popUpTo<TerminateInsuranceDestination.TerminationSuccess> { inclusive = true }
            },
          )
        }
      },
    )
  }
  composable<TerminateInsuranceDestination.TerminationFailure> { backStackEntry ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      openChat = { openChat(backStackEntry) },
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  composable<TerminateInsuranceDestination.UnknownScreen> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  navigation<TerminateInsuranceGraphDestination>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.terminateInsurance },
    ),
  ) {
    composable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsuranceGraphDestination = navController
        .getRouteFromBackStack<TerminateInsuranceGraphDestination>(backStackEntry)
      val viewModel: ChooseInsuranceToTerminateViewModel = koinViewModel {
        parametersOf(terminateInsuranceGraphDestination.insuranceId)
      }
      ChooseInsuranceToTerminateDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        openChat = { openChat(backStackEntry) },
        closeTerminationFlow = closeTerminationFlow,
        // todo: remove fake navigation!!
        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
          navController.navigate(
            TerminateInsuranceDestination.TerminationSurveyFirstStep(
              listOf(previewReason1.surveyOption, previewReason2.surveyOption, previewReason3.surveyOption),
            ),
          )
        },
//        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
//          navigator.navigateToTerminateFlowDestination(
//            // todo: need refactoring, dragging too many parameters around
//            destination = step.toTerminateInsuranceDestination(
//              insuranceForCancellation.displayName,
//              insuranceForCancellation.contractExposure,
//              insuranceForCancellation.activateFrom,
//              insuranceForCancellation.contractGroup,
//              /**
//               * Another possible solution will be just to make an inner graph to go from Start,
//               * and keep all these arguments common for all the destination inside that inner graph.
//               * To not to drag these three args around from destination to destination
//               */
//            ),
//          )
//        },
      )
    }

    composable<TerminateInsuranceDestination.TerminationSurveyFirstStep> { backStackEntry ->
      val viewModel: TerminationSurveyViewModel = koinViewModel {
        parametersOf(options)
      }
      TerminationSurveyDestination(
        viewModel,
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        navigateToSubOptions = { subOptions ->
          TODO()
        },
        navigateToNextStep = { step ->
          TODO()
        },
        navigateToMovingFlow = {
          TODO()
        },
      )
    }

    composable<TerminateInsuranceDestination.TerminationSurveySecondStep> { backStackEntry ->
      val viewModel: TerminationSurveyViewModel = koinViewModel {
        parametersOf(subOptions)
      }
      TerminationSurveyDestination(
        viewModel,
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
        navigateToSubOptions = null,
        navigateToNextStep = { step ->
          TODO()
        },
        navigateToMovingFlow = {
          TODO()
        },
      )
    }

    composable<TerminateInsuranceDestination.TerminationDate> {
      val viewModel: TerminationDateViewModel = koinViewModel {
        parametersOf(TerminationDataParameters(minDate, maxDate, insuranceDisplayName, exposureName))
      }
      TerminationDateDestination(
        viewModel = viewModel,
        onContinue = { localDate ->
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination(
                localDate,
              ),
              parameters = TerminationConfirmationParameters(
                insuranceDisplayName = insuranceDisplayName,
                exposureName = exposureName,
                activeFrom = activeFrom,
                contractGroup = contractGroup,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    composable<TerminateInsuranceDestination.TerminationConfirmation> { backStackEntry ->
      val viewModel: TerminationConfirmationViewModel = koinViewModel {
        parametersOf(
          terminationType,
        )
      }
      TerminationConfirmationDestination(
        viewModel = viewModel,
        onContinue = viewModel::submitContractTermination,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            destination = terminationStep.toTerminateInsuranceDestination(
              insuranceDisplayName = parameters.insuranceDisplayName,
              exposureName = parameters.exposureName,
              activeFrom = parameters.activeFrom,
              contractGroup = parameters.contractGroup,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }

    composable<TerminateInsuranceDestination.InsuranceDeletion> {
      InsuranceDeletionDestination(
        displayName = insuranceDisplayName,
        exposureName = exposureName,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
              parameters = TerminationConfirmationParameters(
                insuranceDisplayName = insuranceDisplayName,
                exposureName = exposureName,
                activeFrom = activeFrom,
                contractGroup = contractGroup,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }
  }
}

/**
 * If we're going to a terminal destination, pop the termination flow backstack completely before going there.
 */
private fun <T : TerminateInsuranceDestination> Navigator.navigateToTerminateFlowDestination(destination: T) {
  val navOptions = navOptions {
    when (destination) {
      is TerminateInsuranceDestination.TerminationSuccess,
      is TerminateInsuranceDestination.TerminationFailure,
      is TerminateInsuranceDestination.UnknownScreen,
      -> {
        popUpTo<TerminateInsuranceGraphDestination> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigateUnsafe(destination, navOptions)
}

// todo: remove!
private val previewReason1 = TerminationReason(
  TerminationSurveyOption(
    id = "1",
    title = "I'm moving",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "11",
        title = "I'm moving in with someone else",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
      ),
      TerminationSurveyOption(
        id = "12",
        title = "I'm moving abroad",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
      ),
      TerminationSurveyOption(
        id = "23",
        title = "Other",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Action.UpdateAddress,
    feedBackRequired = true,
  ),
  null,
)

private val previewReason2 = TerminationReason(
  TerminationSurveyOption(
    id = "2",
    title = "I got a better offer elsewhere",
    subOptions = listOf(),
    suggestion = null,
    feedBackRequired = true,
  ),
  null,
)

private val previewReason3 = TerminationReason(
  TerminationSurveyOption(
    id = "3",
    title = "I am dissatisfied",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "31",
        title = "I am dissatisfied with the coverage",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
      TerminationSurveyOption(
        id = "32",
        title = "I am dissatisfied with the service",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
    ),
    suggestion = null,
    feedBackRequired = false,
  ),
  null,
)
