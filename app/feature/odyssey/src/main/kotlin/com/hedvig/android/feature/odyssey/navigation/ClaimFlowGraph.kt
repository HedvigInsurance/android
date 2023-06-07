package com.hedvig.android.feature.odyssey.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.odyssey.data.ClaimFlowStep
import com.hedvig.android.feature.odyssey.data.toClaimFlowDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.feature.odyssey.step.honestypledge.HonestyPledgeDestination
import com.hedvig.android.feature.odyssey.step.honestypledge.HonestyPledgeViewModel
import com.hedvig.android.feature.odyssey.step.location.LocationDestination
import com.hedvig.android.feature.odyssey.step.location.LocationViewModel
import com.hedvig.android.feature.odyssey.step.notificationpermission.NotificationPermissionDestination
import com.hedvig.android.feature.odyssey.step.notificationpermission.NotificationPermissionViewModel
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberDestination
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemDestination
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemViewModel
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutDestination
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import com.hedvig.android.feature.odyssey.step.success.ClaimSuccessDestination
import com.hedvig.android.feature.odyssey.step.summary.ClaimSummaryDestination
import com.hedvig.android.feature.odyssey.step.summary.ClaimSummaryViewModel
import com.hedvig.android.feature.odyssey.step.unknownerror.UnknownErrorDestination
import com.hedvig.android.feature.odyssey.step.unknownscreen.UnknownScreenDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
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
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  openPlayStore: () -> Unit,
  navigateUp: () -> Boolean,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
) {
  animatedNavigation<Destinations.ClaimFlow>(
    startDestination = createRoutePattern<ClaimFlowDestination.HonestyPledge>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ClaimFlowDestination.HonestyPledge> {
      val viewModel: HonestyPledgeViewModel = koinViewModel { parametersOf(entryPointId) }
      HonestyPledgeDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        openNotificationPermissionStep = { navController.navigate(ClaimFlowDestination.NotificationPermission) },
        startClaimFlow = { viewModel.startClaimFlow() },
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = { navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.NotificationPermission> {
      val viewModel: NotificationPermissionViewModel = koinViewModel { parametersOf(entryPointId) }
      NotificationPermissionDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        startClaimFlow = { viewModel.startClaimFlow() },
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = { navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.AudioRecording> {
      val viewModel: AudioRecordingViewModel = koinViewModel { parametersOf(flowId, audioContent) }
      AudioRecordingDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        questions = questions,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrence> {
      val dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence = this
      val viewModel: DateOfOccurrenceViewModel = koinViewModel { parametersOf(dateOfOccurrence) }
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
      val dateOfOccurencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation = this
      val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel {
        parametersOf(dateOfOccurencePlusLocation)
      }
      DateOfOccurrencePlusLocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        imageLoader = imageLoader,
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
    animatedComposable<ClaimFlowDestination.SingleItem> {
      val singleItem: ClaimFlowDestination.SingleItem = this
      val viewModel: SingleItemViewModel = koinViewModel { parametersOf(singleItem) }
      SingleItemDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.Summary> {
      val summary: ClaimFlowDestination.Summary = this
      val viewModel: ClaimSummaryViewModel = koinViewModel { parametersOf(summary) }
      ClaimSummaryDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItemCheckout> {
      val singleItemCheckout = this
      val viewModel: SingleItemCheckoutViewModel = koinViewModel { parametersOf(singleItemCheckout) }
      BackHandler { finishClaimFlow() }
      SingleItemCheckoutDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          navController.navigate(claimFlowStep.toClaimFlowDestination())
        },
        navigateToAppUpdateStep = { navController.navigate(ClaimFlowDestination.UpdateApp) },
        navigateBack = finishClaimFlow,
        openChat = openChat,
        exitFlow = finishClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.ClaimSuccess> {
      BackHandler { finishClaimFlow() }
      ClaimSuccessDestination(
        windowSizeClass = windowSizeClass,
        openChat = openChat,
        navigateBack = { finishClaimFlow() },
      )
    }
    animatedComposable<ClaimFlowDestination.UpdateApp> {
      BackHandler { finishClaimFlow() }
      UnknownScreenDestination(
        windowSizeClass = windowSizeClass,
        openPlayStore = openPlayStore,
        navigateBack = finishClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.Failure> {
      BackHandler { finishClaimFlow() } // todo remove if failing becomes a recoverable situation
      UnknownErrorDestination(
        windowSizeClass = windowSizeClass,
        openChat = openChat,
        finishClaimFlow = finishClaimFlow,
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
  }
}
