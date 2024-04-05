package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import androidx.navigation.navigation
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
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.core.Navigator
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
  composable<TerminateInsuranceDestination.TerminationSuccess> { _, destination ->
    TerminationSuccessDestination(
      terminationDate = destination.terminationDate,
      insuranceDisplayName = destination.insuranceDisplayName,
      exposureName = destination.exposureName,
      imageLoader = imageLoader,
      onSurveyClicked = { openUrl(destination.surveyUrl) },
      navigateUp = navigator::navigateUp,
    )
  }
  composable<TerminateInsuranceDestination.TerminationFailure> { backStackEntry, destination ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(destination.message),
      openChat = { openChat(backStackEntry) },
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  composable<TerminateInsuranceDestination.UnknownScreen> { _ ->
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  navigation<TerminateInsuranceFeatureDestination>(
    startDestination = TerminateInsuranceDestination.StartStep::class,
  ) {
    composable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsurance = navController.getRouteFromBackStack<TerminateInsuranceFeatureDestination>(backStackEntry)
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
    composable<TerminateInsuranceDestination.TerminationDate> { _, destination ->
      val viewModel: TerminationDateViewModel = koinViewModel {
        parametersOf(destination.minDate, destination.maxDate)
      }
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
      val terminateInsurance = navController.getRouteFromBackStack<TerminateInsuranceFeatureDestination>(backStackEntry)

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
    composable<TerminateInsuranceDestination.TerminationReview> { backStackEntry, destination ->
      val terminateInsurance = navController.getRouteFromBackStack<TerminateInsuranceFeatureDestination>(backStackEntry)
      val viewModel: TerminationReviewViewModel = koinViewModel {
        parametersOf(destination.terminationType, terminateInsurance)
      }
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
