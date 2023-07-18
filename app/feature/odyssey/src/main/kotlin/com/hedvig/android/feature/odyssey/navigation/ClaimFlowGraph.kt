package com.hedvig.android.feature.odyssey.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.feature.odyssey.step.honestypledge.HonestyPledgeDestination
import com.hedvig.android.feature.odyssey.step.location.LocationDestination
import com.hedvig.android.feature.odyssey.step.location.LocationViewModel
import com.hedvig.android.feature.odyssey.step.notificationpermission.NotificationPermissionDestination
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberDestination
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractDestination
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractViewModel
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemDestination
import com.hedvig.android.feature.odyssey.step.singleitem.SingleItemViewModel
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutDestination
import com.hedvig.android.feature.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import com.hedvig.android.feature.odyssey.step.singleitempayout.SingleItemPayoutDestination
import com.hedvig.android.feature.odyssey.step.singleitempayout.SingleItemPayoutViewModel
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
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  navigateToTriaging: (NavBackStackEntry?) -> Unit,
  openAppSettings: () -> Unit,
  closeClaimFlow: () -> Unit,
  nestedGraphs: NavGraphBuilder.() -> Unit,
) {
  animatedNavigation<AppDestination.ClaimsFlow>(
    startDestination = createRoutePattern<ClaimFlowDestination.HonestyPledge>(),
  ) {
    nestedGraphs()
    animatedComposable<ClaimFlowDestination.HonestyPledge> { backStackEntry ->
      HonestyPledgeDestination(
        windowSizeClass = windowSizeClass,
        openNotificationPermissionStep = {
          with(navigator) { backStackEntry.navigate(ClaimFlowDestination.NotificationPermission) }
        },
        pledgeAccepted = {
          navigateToTriaging(backStackEntry)
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.NotificationPermission> {
      NotificationPermissionDestination(
        windowSizeClass = windowSizeClass,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        onNotificationPermissionDecided = {
          // We need to navigate without checking lifecycle, since we want to navigate after accepting the permission.
          // That dialog showing means that the app is not Resumed and would otherwise make us not navigate.
          navigateToTriaging(null)
        },
        openAppSettings = openAppSettings,
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
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
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
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
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
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
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.Location> { backStackEntry ->
      val viewModel: LocationViewModel = koinViewModel { parametersOf(selectedLocation, locationOptions) }
      LocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.PhoneNumber> { backStackEntry ->
      val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(phoneNumber) }
      PhoneNumberDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItem> { backStackEntry ->
      val singleItem: ClaimFlowDestination.SingleItem = this
      val viewModel: SingleItemViewModel = koinViewModel { parametersOf(singleItem) }
      SingleItemDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
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
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.SingleItemCheckout> { backStackEntry ->
      val singleItemCheckout = this
      val viewModel: SingleItemCheckoutViewModel = koinViewModel { parametersOf(singleItemCheckout) }
      SingleItemCheckoutDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToAppUpdateStep = {
          navigator.navigateToClaimFlowDestination(backStackEntry, ClaimFlowDestination.UpdateApp)
        },
        navigateToPayoutStep = { checkoutMethod ->
          navigator.navigateToClaimFlowDestination(
            backStackEntry = backStackEntry,
            destination = ClaimFlowDestination.SingleItemPayout(checkoutMethod = checkoutMethod),
          )
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.SelectContract> { backStackEntry ->
      val viewModel: SelectContractViewModel = koinViewModel { parametersOf(null, options) }
      SelectContractDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
  }
}

/**
 * The list of destinations that are terminal, where the claims flow is already finished. Split in a separate
 * NavGraphBuilder function so that they're not under the AppDestination.ClaimsFlow parent graph.
 */
fun NavGraphBuilder.terminalClaimFlowStepDestinations(
  navigator: Navigator,
  openPlayStore: () -> Unit,
  openChat: () -> Unit,
) {
  animatedComposable<ClaimFlowDestination.SingleItemPayout> {
    val singleItemPayout = this
    val viewModel: SingleItemPayoutViewModel = koinViewModel { parametersOf(singleItemPayout) }
    SingleItemPayoutDestination(
      viewModel = viewModel,
      onDoneAfterPayout = navigator::popBackStack,
      openChat = openChat,
      closePayoutScreen = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.ClaimSuccess> {
    ClaimSuccessDestination(
      openChat = openChat,
      closeSuccessScreen = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.UpdateApp> {
    UnknownScreenDestination(
      openPlayStore = openPlayStore,
      closeUnknownScreenDestination = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.Failure> {
    UnknownErrorDestination(
      openChat = openChat,
      closeFailureScreenDestination = navigator::popBackStack,
    )
  }
}

/**
 * If we're going to a terminal destination, pop the claims flow backstack completely before going there.
 */
fun <T : ClaimFlowDestination> Navigator.navigateToClaimFlowDestination(
  backStackEntry: NavBackStackEntry,
  destination: T,
) {
  val navOptions = navOptions {
    when {
      destination is ClaimFlowDestination.ClaimSuccess ||
        destination is ClaimFlowDestination.UpdateApp ||
        destination is ClaimFlowDestination.Failure ||
        destination is ClaimFlowDestination.SingleItemPayout -> {
        popUpTo<AppDestination.ClaimsFlow> {
          inclusive = true
        }
      }
      else -> {}
    }
  }
  backStackEntry.navigate(destination, navOptions)
}
