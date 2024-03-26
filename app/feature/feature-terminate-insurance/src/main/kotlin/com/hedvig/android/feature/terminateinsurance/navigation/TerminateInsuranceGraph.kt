package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import coil.ImageLoader
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationReviewDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationReviewViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  imageLoader: ImageLoader,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptions, NavBackStackEntry) -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  composable<TerminateInsuranceDestination.TerminationSuccess> { backStackEntry ->
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onSurveyClicked = { openUrl(surveyUrl) },
      navigateToInsurances = {
        val navOptions = NavOptions.Builder()
          .setPopUpTo(createRoutePattern<AppDestination.TopLevelDestination.Home>(), inclusive = false).build()
        navigateToInsurances(navOptions, backStackEntry)
        /**
         We clear everything except for Home destination and navigate to Insurances when we press Done on success screen.
         If we go back (system back button) we return to screen we we've been before termination flow (Help Center or Insurance Details).
         If we successfully delete insurance that was supposed to activate in the future and press system back button on Success screen,
         We'll get a "can't find this insurance" error on Insurance Details screen, which I think is logical.
         */
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
  navigation<AppDestination.TerminationFlow>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.terminateInsurance },
    ),
  ) {
    composable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)
      val insuranceId = terminateInsurance.insuranceId
      val viewModel: ChooseInsuranceToTerminateViewModel = koinViewModel { parametersOf(insuranceId) }
      ChooseInsuranceToTerminateDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        openChat = { openChat(backStackEntry) },
        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(
              insuranceForCancellation.displayName,
              insuranceForCancellation.contractExposure,
              insuranceForCancellation.activateFrom,
              insuranceForCancellation.contractGroup,
              /**
               * Another possible solution will be just to make an inner graph to go from Start,
               * and keep all these arguments common for all the destination inside that inner graph.
               * To not to drag these three args around from destination to destination
               */
            ),
          )
        },
      )
    }

    composable<TerminateInsuranceDestination.TerminationDate> {
      val viewModel: TerminationDateViewModel = koinViewModel { parametersOf(minDate, maxDate) }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        onContinue = { localDate ->
          navController.navigate(
            TerminateInsuranceDestination.TerminationReview(
              TerminationReviewViewModelParameters(
                exposureName = exposureName,
                insuranceDisplayName = insuranceDisplayName,
                terminationType = TerminateInsuranceDestination.TerminationReview.TerminationType.Termination(
                  localDate,
                ),
                contractGroup = contractGroup,
                activeFrom = activeFrom,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }

    composable<TerminateInsuranceDestination.TerminationReview> { backStackEntry ->

      val viewModel: TerminationReviewViewModel = koinViewModel {
        parametersOf(
          parameters,
        )
      }
      TerminationReviewDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
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
        navigateBack = {
          closeTerminationFlow()
        },
      )
    }

    composable<TerminateInsuranceDestination.InsuranceDeletion> { _ ->
      InsuranceDeletionDestination(
        activeFrom = activeFrom,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationReview(
              TerminationReviewViewModelParameters(
                exposureName = exposureName,
                insuranceDisplayName = insuranceDisplayName,
                terminationType = TerminateInsuranceDestination.TerminationReview.TerminationType.Deletion,
                contractGroup = contractGroup,
                activeFrom = activeFrom,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }
  }
}

@Composable
private fun getTerminateInsuranceDataFromParentBackstack(
  navController: NavController,
  backStackEntry: NavBackStackEntry,
): AppDestination.TerminationFlow {
  return remember(navController, backStackEntry) {
    val terminateInsuranceEntry = navController.getBackStackEntry(
      createRoutePattern<AppDestination.TerminationFlow>(),
    )
    decodeArguments(AppDestination.TerminationFlow.serializer(), terminateInsuranceEntry)
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
        popUpTo<TerminateInsuranceDestination.StartStep> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigateUnsafe(destination, navOptions)
}
