package com.hedvig.android.feature.odyssey.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import androidx.navigation.navigation
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingDestination
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrence.DateOfOccurrenceViewModel
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationDestination
import com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationViewModel
import com.hedvig.android.feature.odyssey.step.fileupload.FileUploadDestination
import com.hedvig.android.feature.odyssey.step.fileupload.FileUploadViewModel
import com.hedvig.android.feature.odyssey.step.honestypledge.HonestyPledgeDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.ConfirmEmergencyDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.ConfirmEmergencyViewModel
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectCarOtherDamageDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectEmergencyDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectGlassDamageDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectPestsDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectTowingDestination
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
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  appPackageId: String,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  navigateToTriaging: () -> Unit,
  openAppSettings: () -> Unit,
  closeClaimFlow: () -> Unit,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  nestedGraphs: NavGraphBuilder.() -> Unit,
) {
  navigation<AppDestination.ClaimsFlow>(
    startDestination = ClaimFlowDestination.HonestyPledge::class,
  ) {
    nestedGraphs()
    composable<ClaimFlowDestination.HonestyPledge>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      HonestyPledgeDestination(
        windowSizeClass = windowSizeClass,
        openNotificationPermissionStep = {
          with(navigator) { backStackEntry.navigate(ClaimFlowDestination.NotificationPermission) }
        },
        pledgeAccepted = {
          navigateToTriaging()
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    composable<ClaimFlowDestination.NotificationPermission> { _ ->
      NotificationPermissionDestination(
        windowSizeClass = windowSizeClass,
        onNotificationPermissionDecided = {
          navigateToTriaging()
        },
        openAppSettings = openAppSettings,
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    composable<ClaimFlowDestination.AudioRecording> { backStackEntry, destination ->
      val viewModel: AudioRecordingViewModel = koinViewModel {
        parametersOf(destination.flowId, destination.audioContent)
      }
      AudioRecordingDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        questions = destination.questions,
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
    composable<ClaimFlowDestination.DateOfOccurrence> { backStackEntry, destination ->
      val dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence = destination
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
    composable<ClaimFlowDestination.DateOfOccurrencePlusLocation> { backStackEntry, destination ->
      val dateOfOccurencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation = destination
      val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel { parametersOf(dateOfOccurencePlusLocation) }
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
    composable<ClaimFlowDestination.Location> { backStackEntry, destination ->
      val viewModel: LocationViewModel = koinViewModel {
        parametersOf(destination.selectedLocation, destination.locationOptions)
      }
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
    composable<ClaimFlowDestination.PhoneNumber> { backStackEntry, destination ->
      val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(destination.phoneNumber) }
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
    composable<ClaimFlowDestination.SingleItem> { backStackEntry, destination ->
      val singleItem: ClaimFlowDestination.SingleItem = destination
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
    composable<ClaimFlowDestination.Summary> { backStackEntry, destination ->
      val summary: ClaimFlowDestination.Summary = destination
      val viewModel: ClaimSummaryViewModel = koinViewModel { parametersOf(summary) }
      ClaimSummaryDestination(
        viewModel = viewModel,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
        windowSizeClass = windowSizeClass,
      )
    }
    composable<ClaimFlowDestination.SingleItemCheckout> { backStackEntry ->
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
    composable<ClaimFlowDestination.SelectContract> { backStackEntry, destination ->
      val selectContract: ClaimFlowDestination.SelectContract = destination
      val viewModel: SelectContractViewModel = koinViewModel { parametersOf(selectContract) }
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
    composable<ClaimFlowDestination.ConfirmEmergency> { backStackEntry, destination ->
      val confirmEmergency: ClaimFlowDestination.ConfirmEmergency = destination
      val viewModel: ConfirmEmergencyViewModel = koinViewModel { parametersOf(confirmEmergency) }
      ConfirmEmergencyDestination(
        viewModel = viewModel,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        windowSizeClass = windowSizeClass,
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    composable<ClaimFlowDestination.DeflectGlassDamage> { navBackStackEntry, destination ->
      DeflectGlassDamageDestination(
        deflectGlassDamage = destination,
        openChat = {
          openChat(navBackStackEntry)
        },
        windowSizeClass = windowSizeClass,
        navigateUp = navigator::navigateUp,
        openUrl = openUrl,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }
    composable<ClaimFlowDestination.DeflectTowing> { navBackStackEntry, destination ->
      DeflectTowingDestination(
        deflectTowing = destination,
        openChat = {
          openChat(navBackStackEntry)
        },
        windowSizeClass = windowSizeClass,
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }

    composable<ClaimFlowDestination.DeflectCarOtherDamage> { _, destination ->
      DeflectCarOtherDamageDestination(
        deflectCarOtherDamage = destination,
        windowSizeClass = windowSizeClass,
        navigateUp = navigator::navigateUp,
        closeClaimFlow = closeClaimFlow,
        openUrl = openUrl,
      )
    }

    composable<ClaimFlowDestination.DeflectEmergency> { _, destination ->
      DeflectEmergencyDestination(
        deflectEmergency = destination,
        navigateUp = navigator::navigateUp,
      )
    }
    composable<ClaimFlowDestination.DeflectPests> { navBackStackEntry, destination ->
      DeflectPestsDestination(
        deflectPests = destination,
        openChat = { openChat(navBackStackEntry) },
        navigateUp = navigator::navigateUp,
        openUrl = openUrl,
        windowSizeClass = windowSizeClass,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }
    composable<ClaimFlowDestination.FileUpload> { backStackEntry, destination ->
      val fileUpload: ClaimFlowDestination.FileUpload = destination
      val viewModel: FileUploadViewModel = koinViewModel { parametersOf(fileUpload) }
      FileUploadDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        windowSizeClass = windowSizeClass,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        appPackageId = appPackageId,
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
  openChat: (NavBackStackEntry) -> Unit,
) {
  composable<ClaimFlowDestination.SingleItemPayout> { backStackEntry, destination ->
    val singleItemPayout: ClaimFlowDestination.SingleItemPayout = destination
    val viewModel: SingleItemPayoutViewModel = koinViewModel { parametersOf(singleItemPayout) }
    SingleItemPayoutDestination(
      viewModel = viewModel,
      onDoneAfterPayout = navigator::popBackStack,
      openChat = { openChat(backStackEntry) },
      closePayoutScreen = navigator::popBackStack,
    )
  }
  composable<ClaimFlowDestination.ClaimSuccess> { backStackEntry ->
    ClaimSuccessDestination(
      openChat = { openChat(backStackEntry) },
      closeSuccessScreen = navigator::popBackStack,
    )
  }
  composable<ClaimFlowDestination.UpdateApp> { _ ->
    UnknownScreenDestination(
      openPlayStore = openPlayStore,
      closeUnknownScreenDestination = navigator::popBackStack,
    )
  }
  composable<ClaimFlowDestination.Failure> { backStackEntry ->
    UnknownErrorDestination(
      openChat = { openChat(backStackEntry) },
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
    when (destination) {
      is ClaimFlowDestination.ClaimSuccess,
      is ClaimFlowDestination.UpdateApp,
      is ClaimFlowDestination.Failure,
      is ClaimFlowDestination.SingleItemPayout,
      -> {
        popUpTo<AppDestination.ClaimsFlow> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  backStackEntry.navigate(destination, navOptions)
}
