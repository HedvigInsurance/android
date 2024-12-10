package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
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
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  navigateToMovingFlow: () -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptionsBuilder.() -> Unit) -> Unit,
  closeTerminationFlow: () -> Unit,
  redirectToChangeTierFlow: (NavBackStackEntry, Pair<String, ChangeTierDeductibleIntent>) -> Unit,
) {
  navdestination<TerminateInsuranceDestination.TerminationFailure> { backStackEntry ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
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

  navdestination<TerminateInsuranceDestination.TerminationSuccess>(
    TerminateInsuranceDestination.TerminationSuccess,
  ) {
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onDone = {
        if (!navController.popBackStack()) {
          // In the deep link situation, we want to navigate to Insurances when we're successfully done with this flow
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
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.terminateInsurance },
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
        navigateUp = navigator::navigateUp,
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
        closeTerminationFlow = closeTerminationFlow,
        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
          val commonParams = TerminationGraphParameters(
            insuranceForCancellation.displayName,
            insuranceForCancellation.contractExposure,
            insuranceForCancellation.contractGroup,
          )
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(commonParams),
          )
        },
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationSurveyFirstStep>(
      TerminateInsuranceDestination.TerminationSurveyFirstStep,
    ) { backStackEntry ->
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
        navigateToMovingFlow = navigateToMovingFlow,
        openUrl = openUrl,
        redirectToChangeTierFlow = { intent ->
          redirectToChangeTierFlow(backStackEntry, intent)
        },
      )
    }

    navdestination<TerminateInsuranceDestination.TerminationSurveySecondStep>(
      TerminateInsuranceDestination.TerminationSurveySecondStep,
    ) { backStackEntry ->
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
        navigateToMovingFlow = navigateToMovingFlow,
        openUrl = openUrl,
        redirectToChangeTierFlow = { intent ->
          redirectToChangeTierFlow(backStackEntry, intent)
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
            ),
          )
        },
        navigateUp = navigator::navigateUp,
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
            ),
          )
        },
        navigateUp = navigator::navigateUp,
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
        closeTerminationFlow = closeTerminationFlow
      )
    }
  }
}

/**
 * If we're going to a terminal destination, pop the termination flow backstack completely before going there.
 */
private fun <T : Destination> Navigator.navigateToTerminateFlowDestination(destination: T) {
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
  navigateUnsafe(destination, navOptions)
}
