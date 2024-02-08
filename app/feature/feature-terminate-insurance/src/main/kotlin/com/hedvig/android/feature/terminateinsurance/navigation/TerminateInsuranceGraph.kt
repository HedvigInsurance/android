package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import coil.ImageLoader
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartDestination
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationReviewDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationReviewViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * This flow does not have a nice start destination, so the back navigation is all messed up, with having to manually
 * check if we're going back to [TerminateInsuranceDestination.StartStep] which is a "fake" step which only launches
 * the backend driven flow. This will stay this way until a more "stable" start destination can be made, potentially
 * one which gives a brief explanation of what the flow is going to be about or something like that.
 *
 * The changes that could be added after this is fixed is to use navigator::navigateUp for all "up" actions, and remove
 * all instances of [BackHandler].
 */
fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  imageLoader: ImageLoader,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  openPlayStore: () -> Unit,
) {
  composable<TerminateInsuranceDestination.TerminationSuccess> {
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      insuranceDisplayName = insuranceDisplayName,
      exposureName = exposureName,
      imageLoader = imageLoader,
      onSurveyClicked = { openUrl(surveyUrl) },
      navigateUp = navigator::navigateUp,
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
  navigation<TerminateInsuranceFeatureDestination>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
  ) {
    composable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)
      val insuranceId = InsuranceId(terminateInsurance.contractId)
      val viewModel: TerminationStartStepViewModel = koinViewModel { parametersOf(insuranceId) }
      TerminationStartDestination(
        viewModel = viewModel,
        insuranceDisplayName = terminateInsurance.insuranceDisplayName,
        exposureName = terminateInsurance.exposureName,
        contractGroup = terminateInsurance.contractGroup,
        imageLoader = imageLoader,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
        navigateToNextStep = { terminationStep ->
          navigator.navigateToTerminateFlowDestination(
            destination = terminationStep.toTerminateInsuranceDestination(
              terminateInsurance.insuranceDisplayName,
              terminateInsurance.exposureName,
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
              TerminateInsuranceDestination.TerminationReview.TerminationType.Termination(localDate),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<TerminateInsuranceDestination.InsuranceDeletion> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)

      InsuranceDeletionDestination(
        activeFrom = terminateInsurance.activeFrom,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationReview(
              TerminateInsuranceDestination.TerminationReview.TerminationType.Deletion,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<TerminateInsuranceDestination.TerminationReview> { backStackEntry ->
      val terminateInsurance = getTerminateInsuranceDataFromParentBackstack(navController, backStackEntry)
      val viewModel: TerminationReviewViewModel = koinViewModel { parametersOf(terminationType, terminateInsurance) }
      TerminationReviewDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
        onContinue = viewModel::submitContractTermination,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            destination = terminationStep.toTerminateInsuranceDestination(
              terminateInsurance.insuranceDisplayName,
              terminateInsurance.exposureName,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
  }
}

@Composable
private fun getTerminateInsuranceDataFromParentBackstack(
  navController: NavController,
  backStackEntry: NavBackStackEntry,
): TerminateInsuranceFeatureDestination {
  return remember(navController, backStackEntry) {
    val terminateInsuranceEntry = navController.getBackStackEntry(
      createRoutePattern<TerminateInsuranceFeatureDestination>(),
    )
    decodeArguments(TerminateInsuranceFeatureDestination.serializer(), terminateInsuranceEntry)
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
        popUpTo<TerminateInsuranceFeatureDestination> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigateUnsafe(destination, navOptions)
}
