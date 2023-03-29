package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartDestination
import com.hedvig.android.feature.terminateinsurance.step.start.TerminationStartStepViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  insuranceId: InsuranceId,
  insuranceDisplayName: String,
  navigateUp: () -> Boolean,
  openChat: () -> Unit,
  openPlayStore: () -> Unit,
  finishTerminationFlow: () -> Unit,
) {
  animatedNavigation<Destinations.TerminateInsurance>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<TerminateInsuranceDestination.StartStep> {
      val viewModel: TerminationStartStepViewModel = koinViewModel { parametersOf(insuranceId) }
      TerminationStartDestination(
        viewModel = viewModel,
        retryLoad = { viewModel.retryToStartTerminationFlow() },
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(terminationStep.toTerminateInsuranceDestination())
        },
      )
    }
    animatedComposable<TerminateInsuranceDestination.TerminationDate> {
      val shouldFinishFlowOnBack = run {
        val previousBackstackEntryRoute = navController.previousBackStackEntry?.destination?.route
        previousBackstackEntryRoute == createRoutePattern<TerminateInsuranceDestination.StartStep>()
      }
      BackHandler(shouldFinishFlowOnBack) {
        finishTerminationFlow()
      }
      val viewModel: TerminationDateViewModel = koinViewModel { parametersOf(this.minDate, this.maxDate) }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(terminationStep.toTerminateInsuranceDestination())
        },
        navigateBack = {
          if (shouldFinishFlowOnBack) {
            finishTerminationFlow()
          } else {
            navController.navigateUp() || navigateUp()
          }
        },
      )
    }
    animatedComposable<TerminateInsuranceDestination.TerminationSuccess> {
      BackHandler { finishTerminationFlow() }
      TerminationSuccessDestination(
        terminationDate = this.terminationDate,
        surveyUrl = this.surveyUrl,
        windowSizeClass = windowSizeClass,
        navigateBack = finishTerminationFlow,
      )
    }
    animatedComposable<TerminateInsuranceDestination.TerminationFailure> {
      BackHandler { finishTerminationFlow() }
      TerminationFailureDestination(
        windowSizeClass = windowSizeClass,
        errorMessage = ErrorMessage(this.message),
        openChat = openChat,
        navigateBack = finishTerminationFlow,
      )
    }
    animatedComposable<TerminateInsuranceDestination.UnknownScreen> {
      BackHandler { finishTerminationFlow() }
      UnknownScreenDestination(
        windowSizeClass = windowSizeClass,
        openPlayStore = openPlayStore,
        navigateBack = finishTerminationFlow,
      )
    }
  }
}
