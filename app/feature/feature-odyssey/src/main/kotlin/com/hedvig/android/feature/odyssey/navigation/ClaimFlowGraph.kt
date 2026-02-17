package com.hedvig.android.feature.odyssey.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import coil3.ImageLoader
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
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
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectIdProtectionDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectPestsDestination
import com.hedvig.android.feature.odyssey.step.informdeflect.DeflectTowingDestination
import com.hedvig.android.feature.odyssey.step.location.LocationDestination
import com.hedvig.android.feature.odyssey.step.location.LocationViewModel
import com.hedvig.android.feature.odyssey.step.notificationpermission.NotificationPermissionDestination
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberDestination
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberViewModel
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractDestination
import com.hedvig.android.feature.odyssey.step.selectcontract.SelectContractEvent
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
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  appPackageId: String,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  navigateToTriaging: () -> Unit,
  openAppSettings: () -> Unit,
  closeClaimFlow: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  imageLoader: ImageLoader,
  nestedGraphs: NavGraphBuilder.() -> Unit,
) {
  navgraph<ClaimsFlowGraphDestination>(
    startDestination = ClaimFlowDestination.HonestyPledge::class,
  ) {
    nestedGraphs()
    navdestination<ClaimFlowDestination.HonestyPledge>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.claimFlow),
    ) { backStackEntry ->
      HonestyPledgeDestination(
        windowSizeClass = windowSizeClass,
        openNotificationPermissionStep = {
          navController.navigate(ClaimFlowDestination.NotificationPermission)
        },
        pledgeAccepted = {
          navigateToTriaging()
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.NotificationPermission> {
      NotificationPermissionDestination(
        windowSizeClass = windowSizeClass,
        onNotificationPermissionDecided = {
          navigateToTriaging()
        },
        openAppSettings = openAppSettings,
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.AudioRecording>(
      ClaimFlowDestination.AudioRecording,
    ) { backStackEntry ->
      val audioRecording = this
      val viewModel: AudioRecordingViewModel = koinViewModel { parametersOf(audioRecording) }
      AudioRecordingDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        questions = questions,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
        freeTextAvailable = this.freeTextAvailable,
        freeTextQuestions = this.freeTextQuestions,
      )
    }
    navdestination<ClaimFlowDestination.DateOfOccurrence>(
      ClaimFlowDestination.DateOfOccurrence,
    ) { backStackEntry ->
      val dateOfOccurrence: ClaimFlowDestination.DateOfOccurrence = this
      val viewModel: DateOfOccurrenceViewModel = koinViewModel { parametersOf(dateOfOccurrence) }
      DateOfOccurrenceDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep: ClaimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.DateOfOccurrencePlusLocation>(
      ClaimFlowDestination.DateOfOccurrencePlusLocation,
    ) { backStackEntry ->
      val dateOfOccurencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation = this
      val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel {
        parametersOf(dateOfOccurencePlusLocation)
      }
      DateOfOccurrencePlusLocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateBack = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.Location>(
      ClaimFlowDestination.Location,
    ) { backStackEntry ->
      val viewModel: LocationViewModel = koinViewModel { parametersOf(selectedLocation, locationOptions) }
      LocationDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.PhoneNumber> { backStackEntry ->
      val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(phoneNumber) }
      PhoneNumberDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.SingleItem>(
      ClaimFlowDestination.SingleItem,
    ) { backStackEntry ->
      val singleItem: ClaimFlowDestination.SingleItem = this
      val viewModel: SingleItemViewModel = koinViewModel { parametersOf(singleItem) }
      SingleItemDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.Summary>(
      ClaimFlowDestination.Summary,
    ) { backStackEntry ->
      val summary: ClaimFlowDestination.Summary = this
      val viewModel: ClaimSummaryViewModel = koinViewModel { parametersOf(summary) }
      ClaimSummaryDestination(
        viewModel = viewModel,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
        onNavigateToImageViewer = onNavigateToImageViewer,
        imageLoader = imageLoader,
        windowSizeClass = windowSizeClass,
      )
    }
    navdestination<ClaimFlowDestination.SingleItemCheckout>(
      ClaimFlowDestination.SingleItemCheckout,
    ) { backStackEntry ->
      val singleItemCheckout = this
      val viewModel: SingleItemCheckoutViewModel = koinViewModel { parametersOf(singleItemCheckout) }
      SingleItemCheckoutDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToAppUpdateStep = {
          navController.navigateToClaimFlowDestination( ClaimFlowDestination.UpdateApp)
        },
        navigateToPayoutStep = { checkoutMethod ->
          navController.navigateToClaimFlowDestination(
            destination = ClaimFlowDestination.SingleItemPayout(checkoutMethod = checkoutMethod),
          )
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.SelectContract>(
      ClaimFlowDestination.SelectContract,
    ) { backStackEntry ->
      val viewModel: SelectContractViewModel = koinViewModel { parametersOf(this) }
      SelectContractDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToNextStep = { claimFlowStep ->
          viewModel.emit(SelectContractEvent.HandledNextStepNavigation)
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.ConfirmEmergency>(
      ClaimFlowDestination.ConfirmEmergency,
    ) { backStackEntry ->
      val viewModel: ConfirmEmergencyViewModel = koinViewModel { parametersOf(this) }
      ConfirmEmergencyDestination(
        viewModel = viewModel,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        windowSizeClass = windowSizeClass,
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
      )
    }
    navdestination<ClaimFlowDestination.DeflectGlassDamage>(
      ClaimFlowDestination.DeflectGlassDamage,
    ) {
      DeflectGlassDamageDestination(
        deflectGlassDamage = this,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        windowSizeClass = windowSizeClass,
        navigateUp = navController::navigateUp,
        openUrl = openUrl,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }
    navdestination<ClaimFlowDestination.DeflectTowing>(
      ClaimFlowDestination.DeflectTowing,
    ) {
      DeflectTowingDestination(
        deflectTowing = this,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        windowSizeClass = windowSizeClass,
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }

    navdestination<ClaimFlowDestination.DeflectCarOtherDamage>(
      ClaimFlowDestination.DeflectCarOtherDamage,
    ) {
      DeflectCarOtherDamageDestination(
        deflectCarOtherDamage = this,
        windowSizeClass = windowSizeClass,
        navigateUp = navController::navigateUp,
        closeClaimFlow = closeClaimFlow,
        openUrl = openUrl,
      )
    }

    navdestination<ClaimFlowDestination.DeflectEmergency>(
      ClaimFlowDestination.DeflectEmergency,
    ) {
      DeflectEmergencyDestination(
        deflectEmergency = this,
        navigateUp = navController::navigateUp,
        openUrl = openUrl,
        tryToDialPhone = tryToDialPhone,
      )
    }
    navdestination<ClaimFlowDestination.DeflectPests>(
      ClaimFlowDestination.DeflectPests,
    ) {
      DeflectPestsDestination(
        deflectPests = this,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        navigateUp = navController::navigateUp,
        openUrl = openUrl,
        windowSizeClass = windowSizeClass,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }
    navdestination<ClaimFlowDestination.DeflectIdProtection>(
      ClaimFlowDestination.DeflectIdProtection,
    ) {
      DeflectIdProtectionDestination(
        deflectIdProtection = this,
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        navigateUp = navController::navigateUp,
        windowSizeClass = windowSizeClass,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
      )
    }
    navdestination<ClaimFlowDestination.FileUpload>(
      ClaimFlowDestination.FileUpload,
    ) { backStackEntry ->
      val viewModel: FileUploadViewModel = koinViewModel { parametersOf(this) }
      FileUploadDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        windowSizeClass = windowSizeClass,
        closeClaimFlow = closeClaimFlow,
        imageLoader = imageLoader,
        navigateToNextStep = { claimFlowStep ->
          viewModel.handledNextStepNavigation()
          navController.navigateToClaimFlowDestination( claimFlowStep.toClaimFlowDestination())
        },
        onNavigateToImageViewer = onNavigateToImageViewer,
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
  navController: NavController,
  openPlayStore: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  navdestination<ClaimFlowDestination.SingleItemPayout>(
    ClaimFlowDestination.SingleItemPayout,
  ) {
    val singleItemPayout = this
    val viewModel: SingleItemPayoutViewModel = koinViewModel { parametersOf(singleItemPayout) }
    SingleItemPayoutDestination(
      viewModel = viewModel,
      onDoneAfterPayout = navController::popBackStack,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      closePayoutScreen = navController::popBackStack,
    )
  }
  navdestination<ClaimFlowDestination.ClaimSuccess> {
    ClaimSuccessDestination(
      closeSuccessScreen = navController::popBackStack,
    )
  }
  navdestination<ClaimFlowDestination.UpdateApp> {
    UnknownScreenDestination(
      openPlayStore = openPlayStore,
      closeUnknownScreenDestination = navController::popBackStack,
    )
  }
  navdestination<ClaimFlowDestination.Failure> {
    UnknownErrorDestination(
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      closeFailureScreenDestination = navController::popBackStack,
    )
  }
}

/**
 * If we're going to a terminal destination, pop the claims flow backstack completely before going there.
 */
fun NavController.navigateToClaimFlowDestination(destination: Destination) {
  val navOptions: NavOptionsBuilder.() -> Unit = {
    when (destination) {
      is ClaimFlowDestination.ClaimSuccess,
      is ClaimFlowDestination.UpdateApp,
      is ClaimFlowDestination.Failure,
      is ClaimFlowDestination.SingleItemPayout,
      -> {
        typedPopUpTo<ClaimsFlowGraphDestination> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigate(destination, navOptions)
}
