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
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: (NavBackStackEntry) -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptions) -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  composable<TerminateInsuranceDestination.TerminationFailure> { backStackEntry ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
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
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
        closeTerminationFlow = closeTerminationFlow,
        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
          val commonParams = TerminationGraphParameters(
            insuranceForCancellation.displayName,
            insuranceForCancellation.contractExposure,
          )
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(commonParams),
          )
        },
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
          navController.navigate(
            TerminateInsuranceDestination.TerminationSurveySecondStep(subOptions, commonParams),
          )
        },
        navigateToNextStep = { step ->
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(commonParams),
          )
        },
        navigateToMovingFlow = { navigateToMovingFlow(backStackEntry) },
        openUrl = openUrl,
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
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(commonParams),
          )
        },
        navigateToMovingFlow = { navigateToMovingFlow(backStackEntry) },
        openUrl = openUrl,
      )
    }

    composable<TerminateInsuranceDestination.TerminationDate> { backStackEntry ->
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
              commonParams,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    composable<TerminateInsuranceDestination.InsuranceDeletion> { backStackEntry ->
      InsuranceDeletionDestination(
        displayName = commonParams.insuranceDisplayName,
        exposureName = commonParams.exposureName,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
              commonParams,
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
            destination = terminationStep.toTerminateInsuranceDestination(commonParams),
          )
        },
        navigateUp = navigator::navigateUp,
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
