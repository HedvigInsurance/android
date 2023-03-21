package com.hedvig.android.odyssey.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
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
import com.hedvig.android.odyssey.step.singleitempayout.SingleItemPayoutDestination
import com.hedvig.android.odyssey.step.start.ClaimFlowStartDestination
import com.hedvig.android.odyssey.step.start.ClaimFlowStartStepViewModel
import com.hedvig.android.odyssey.step.unknown.UnknownScreenDestination
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
//      val viewModel: SingleItemViewModel = koinViewModel()
      SingleItemDestination(
        imageLoader = imageLoader,
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItemPayout> {
      SingleItemPayoutDestination()
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
  }
}
