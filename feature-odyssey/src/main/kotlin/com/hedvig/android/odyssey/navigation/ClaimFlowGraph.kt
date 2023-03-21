package com.hedvig.android.odyssey.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.data.toClaimFlowDestination
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingDestination
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.odyssey.step.dateofoccurrence.DateOfOccurrenceDestination
import com.hedvig.android.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationDestination
import com.hedvig.android.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.odyssey.step.location.LocationDestination
import com.hedvig.android.odyssey.step.location.LocationViewModel
import com.hedvig.android.odyssey.step.manualhandling.ManualHandlingDestination
import com.hedvig.android.odyssey.step.phonenumber.PhoneNumberDestination
import com.hedvig.android.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.odyssey.step.singleitem.SingleItemDestination
import com.hedvig.android.odyssey.step.singleitempayout.ClaimSuccessDestination
import com.hedvig.android.odyssey.step.start.ClaimFlowStartDestination
import com.hedvig.android.odyssey.step.start.ClaimFlowStartStepViewModel
import com.hedvig.android.odyssey.step.unknownerror.UnknownErrorDestination
import com.hedvig.android.odyssey.step.unknownscreen.UnknownScreenDestination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  imageLoader: ImageLoader,
  entryPointId: String?,
  openAppSettings: () -> Unit,
  navigateUp: () -> Boolean,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
) {
  animatedNavigation<Destinations.ClaimFlow>(
    startDestination = createRoutePattern<ClaimFlowDestination.StartStep>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ClaimFlowDestination.StartStep> {
      val viewModel: ClaimFlowStartStepViewModel = koinViewModel { parametersOf(entryPointId) }
      ClaimFlowStartDestination(
        viewModel = viewModel,
        retryLoad = { viewModel.retryToStartClaimFlow() },
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
      )
    }
    animatedComposable<ClaimFlowDestination.AudioRecording> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
      val viewModel: AudioRecordingViewModel = koinViewModel { parametersOf(flowId) }
      AudioRecordingDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        questions = questions,
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrence> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
      val viewModel: DateOfOccurrenceViewModel = koinViewModel { parametersOf(dateOfOccurrence, maxDate) }
      DateOfOccurrenceDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep: ClaimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrencePlusLocation> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
      val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel {
        parametersOf(dateOfOccurrence, maxDate, selectedLocation, locationOptions)
      }
      DateOfOccurrencePlusLocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.Location> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
      val viewModel: LocationViewModel = koinViewModel { parametersOf(selectedLocation, locationOptions) }
      LocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.PhoneNumber> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
      val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(phoneNumber) }
      PhoneNumberDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    // todo these screens below here aren't implemented yet. Also maybe there are some missing
//    animatedComposable<ClaimFlowDestination.ClaimSummary> {
//      val viewModel: ClaimSummaryViewModel = koinViewModel()
//      ClaimSummaryDestination(imageLoader = imageLoader)
//    }
    animatedComposable<ClaimFlowDestination.SingleItem> {
      ClaimFlowBackHandler(navController, finishClaimFlow)
//      val viewModel: SingleItemViewModel = koinViewModel()
      SingleItemDestination(
        imageLoader = imageLoader,
      )
    }
    animatedComposable<ClaimFlowDestination.ClaimSuccess> {
      BackHandler { finishClaimFlow() }
      // todo backend for success returns nothing but an ID. Not sure if it's only missing on the backend or if it's a
      //  different screen.
      ClaimSuccessDestination()
    }
    animatedComposable<ClaimFlowDestination.ManualHandling> {
      BackHandler { finishClaimFlow() }
//      val viewModel: ManualHandlingViewModel = koinViewModel()
      ManualHandlingDestination(
        navigateUp = finishClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.UnknownScreen> {
      BackHandler { finishClaimFlow() }
      UnknownScreenDestination(
        openChat = openChat,
        navigateBack = finishClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.Failure> {
      BackHandler { finishClaimFlow() }
      UnknownErrorDestination(
        windowSizeClass = windowSizeClass,
        openChat = openChat,
        navigateBack = finishClaimFlow,
      )
    }
  }
}

/**
 * If the previous entry is the start step, which is meant to only start the flow and move to the next step, skip that
 * and finish the entire flow instead.
 * This workaround should be deleted once and if this ever becomes part of another navigation graph and the start step
 * can be skipped.
 */
@Composable
private fun ClaimFlowBackHandler(navController: NavController, finishClaimFlow: () -> Unit) {
  val shouldFinishFlowOnBack = run {
    val previousBackstackEntryRoute = navController.previousBackStackEntry?.destination?.route
    previousBackstackEntryRoute == createRoutePattern<ClaimFlowDestination.StartStep>()
  }
  BackHandler(shouldFinishFlowOnBack) {
    finishClaimFlow()
  }
}
