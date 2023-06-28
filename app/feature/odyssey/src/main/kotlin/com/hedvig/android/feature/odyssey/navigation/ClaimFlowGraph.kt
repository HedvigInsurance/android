package com.hedvig.android.feature.odyssey.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
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
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavController,
  navigator: Navigator,
  imageLoader: ImageLoader,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  openPlayStore: () -> Unit,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
) {
  animatedNavigation<AppDestination.ClaimsFlow>(
    startDestination = createRoutePattern<ClaimFlowDestination.HonestyPledge>(),
  ) {
    animatedComposable<ClaimFlowDestination.HonestyPledge> { backStackEntry ->
      val entryPointId = remember(navController, backStackEntry) {
        val claimsFlow: AppDestination.ClaimsFlow = decodeArguments(
          AppDestination.ClaimsFlow.serializer(),
          navController.getBackStackEntry(createRoutePattern<AppDestination.ClaimsFlow>()),
        )
        claimsFlow.entryPointId
      }
      val viewModel: HonestyPledgeViewModel = koinViewModel { parametersOf(entryPointId) }
      HonestyPledgeDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        openNotificationPermissionStep = {
          with(navigator) { backStackEntry.navigate(ClaimFlowDestination.NotificationPermission) }
        },
        startClaimFlow = { viewModel.startClaimFlow() },
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.NotificationPermission> { backStackEntry ->
      val entryPointId = remember(navController, backStackEntry) {
        val claimsFlow: AppDestination.ClaimsFlow = decodeArguments(
          AppDestination.ClaimsFlow.serializer(),
          navController.getBackStackEntry(createRoutePattern<AppDestination.ClaimsFlow>()),
        )
        claimsFlow.entryPointId
      }
      val viewModel: NotificationPermissionViewModel = koinViewModel { parametersOf(entryPointId) }
      NotificationPermissionDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        startClaimFlow = { viewModel.startClaimFlow() },
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.AudioRecording> { backStackEntry ->
      val viewModel: AudioRecordingViewModel = koinViewModel { parametersOf(flowId, audioContent) }
      AudioRecordingDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        questions = questions,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrence> { backStackEntry ->
      val dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence = this
      val viewModel: DateOfOccurrenceViewModel = koinViewModel { parametersOf(dateOfOccurrence) }
      DateOfOccurrenceDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep: ClaimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateBack = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrencePlusLocation> { backStackEntry ->
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
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateBack = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.Location> { backStackEntry ->
      val viewModel: LocationViewModel = koinViewModel { parametersOf(selectedLocation, locationOptions) }
      LocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.PhoneNumber> { backStackEntry ->
      val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(phoneNumber) }
      PhoneNumberDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItem> { backStackEntry ->
      val singleItem: ClaimFlowDestination.SingleItem = this
      val viewModel: SingleItemViewModel = koinViewModel { parametersOf(singleItem) }
      SingleItemDestination( // todo PriceOfPurchase input text field
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.Summary> { backStackEntry ->
      val summary: ClaimFlowDestination.Summary = this
      val viewModel: ClaimSummaryViewModel = koinViewModel { parametersOf(summary) }
      ClaimSummaryDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItemCheckout> { backStackEntry ->
      val singleItemCheckout = this
      val viewModel: SingleItemCheckoutViewModel = koinViewModel { parametersOf(singleItemCheckout) }
      BackHandler { finishClaimFlow() }
      SingleItemCheckoutDestination( // todo many changes here too
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          with(navigator) { backStackEntry.navigate(claimFlowStep.toClaimFlowDestination()) }
        },
        navigateToAppUpdateStep = {
          with(navigator) { backStackEntry.navigate(ClaimFlowDestination.UpdateApp) }
        },
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
        navigateBack = navigator::navigateUp,
      )
    }
  }
}
