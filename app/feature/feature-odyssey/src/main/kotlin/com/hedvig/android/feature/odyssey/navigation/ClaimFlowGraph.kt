package com.hedvig.android.feature.odyssey.navigation

import com.kiwi.navigationcompose.typed.popUpTo as typedPopUpTo
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navOptions
import androidx.navigation.navigation
import coil.ImageLoader
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
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
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createNavArguments
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.registerDestinationType
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimFlowGraph(
  sharedTransitionScope: SharedTransitionScope,
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
  with(sharedTransitionScope) {
    animatedNavigation<AppDestination.ClaimsFlow>(
      startDestination = createRoutePattern<ClaimFlowDestination.HonestyPledge>(),
    ) {
      nestedGraphs()
      animatedComposable<ClaimFlowDestination.HonestyPledge>(
        enterTransition = { MotionDefaults.fadeThroughEnter },
        exitTransition = { MotionDefaults.fadeThroughExit },
      ) { backStackEntry, _ ->
        HonestyPledgeDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.NotificationPermission> { _, _ ->
        NotificationPermissionDestination(
          animatedContentScope = this,
          windowSizeClass = windowSizeClass,
          onNotificationPermissionDecided = {
            navigateToTriaging()
          },
          openAppSettings = openAppSettings,
          navigateUp = navigator::navigateUp,
          closeClaimFlow = closeClaimFlow,
        )
      }
      animatedComposable<ClaimFlowDestination.AudioRecording> { backStackEntry, destination ->
        val flowId = destination.flowId
        val audioContent = destination.audioContent
        val questions = destination.questions
        val viewModel: AudioRecordingViewModel = koinViewModel { parametersOf(flowId, audioContent) }
        AudioRecordingDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.DateOfOccurrence> { backStackEntry, dateOfOccurrence ->
        val viewModel: DateOfOccurrenceViewModel = koinViewModel { parametersOf(dateOfOccurrence) }
        DateOfOccurrenceDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.DateOfOccurrencePlusLocation> { backStackEntry, dateOfOccurencePlusLocation ->
        val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel {
          parametersOf(dateOfOccurencePlusLocation)
        }
        DateOfOccurrencePlusLocationDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.Location> { backStackEntry, destination ->
        val selectedLocation = destination.selectedLocation
        val locationOptions = destination.locationOptions
        val viewModel: LocationViewModel = koinViewModel { parametersOf(selectedLocation, locationOptions) }
        LocationDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.PhoneNumber> { backStackEntry, destination ->
        val viewModel: PhoneNumberViewModel = koinViewModel { parametersOf(destination.phoneNumber) }
        PhoneNumberDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.SingleItem> { backStackEntry, destination ->
        val viewModel: SingleItemViewModel = koinViewModel { parametersOf(destination) }
        SingleItemDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.Summary> { backStackEntry, destination ->
        val viewModel: ClaimSummaryViewModel = koinViewModel { parametersOf(destination) }
        ClaimSummaryDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.SingleItemCheckout> { backStackEntry, destination ->
        val viewModel: SingleItemCheckoutViewModel = koinViewModel { parametersOf(destination) }
        SingleItemCheckoutDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.SelectContract> { backStackEntry, destination ->
        val viewModel: SelectContractViewModel = koinViewModel { parametersOf(destination) }
        SelectContractDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.ConfirmEmergency> { backStackEntry, destination ->
        val viewModel: ConfirmEmergencyViewModel = koinViewModel { parametersOf(destination) }
        ConfirmEmergencyDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.DeflectGlassDamage> { navBackStackEntry, destination ->
        DeflectGlassDamageDestination(
          animatedContentScope = this,
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
      animatedComposable<ClaimFlowDestination.DeflectTowing> { navBackStackEntry, destination ->
        DeflectTowingDestination(
          animatedContentScope = this,
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

      animatedComposable<ClaimFlowDestination.DeflectCarOtherDamage> { _, destination ->
        DeflectCarOtherDamageDestination(
          animatedContentScope = this,
          deflectCarOtherDamage = destination,
          windowSizeClass = windowSizeClass,
          navigateUp = navigator::navigateUp,
          closeClaimFlow = closeClaimFlow,
          openUrl = openUrl,
        )
      }

      animatedComposable<ClaimFlowDestination.DeflectEmergency> { _, destination ->
        DeflectEmergencyDestination(
          deflectEmergency = destination,
          navigateUp = navigator::navigateUp,
        )
      }
      animatedComposable<ClaimFlowDestination.DeflectPests> { navBackStackEntry, destination ->
        DeflectPestsDestination(
          animatedContentScope = this,
          deflectPests = destination,
          openChat = {
            openChat(navBackStackEntry)
          },
          navigateUp = navigator::navigateUp,
          openUrl = openUrl,
          windowSizeClass = windowSizeClass,
          closeClaimFlow = closeClaimFlow,
          imageLoader = imageLoader,
        )
      }
      animatedComposable<ClaimFlowDestination.FileUpload> { backStackEntry, destination ->
        val viewModel: FileUploadViewModel = koinViewModel { parametersOf(destination) }
        FileUploadDestination(
          animatedContentScope = this,
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
  animatedComposable<ClaimFlowDestination.SingleItemPayout> { backStackEntry, _ ->
    val singleItemPayout = this
    val viewModel: SingleItemPayoutViewModel = koinViewModel { parametersOf(singleItemPayout) }
    SingleItemPayoutDestination(
      viewModel = viewModel,
      onDoneAfterPayout = navigator::popBackStack,
      openChat = { openChat(backStackEntry) },
      closePayoutScreen = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.ClaimSuccess> { backStackEntry, _ ->
    ClaimSuccessDestination(
      openChat = { openChat(backStackEntry) },
      closeSuccessScreen = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.UpdateApp> { _, _ ->
    UnknownScreenDestination(
      openPlayStore = openPlayStore,
      closeUnknownScreenDestination = navigator::popBackStack,
    )
  }
  animatedComposable<ClaimFlowDestination.Failure> { backStackEntry, _ ->
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
        typedPopUpTo<AppDestination.ClaimsFlow> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  backStackEntry.navigate(destination, navOptions)
}

internal inline fun <reified T : Destination> NavGraphBuilder.animatedNavigation(
  startDestination: String,
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  noinline exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  noinline popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  noinline popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  noinline builder: NavGraphBuilder.() -> Unit
) {
  val serializer = serializer<T>()
  registerDestinationType(T::class, serializer)
  navigation(
    startDestination = startDestination,
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer),
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    builder = builder,
  )
}

internal inline fun <reified T : Destination> NavGraphBuilder.animatedComposable(
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  noinline exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  noinline popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  noinline popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  noinline content: @Composable AnimatedContentScope.(NavBackStackEntry, T) -> Unit,
) {
  val serializer = serializer<T>()
  registerDestinationType(T::class, serializer)
  composable(
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer),
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    deepLinks = deepLinks,
  ) { navBackStackEntry ->
    val t = decodeArguments(serializer, navBackStackEntry)
    content(navBackStackEntry, t)
  }
}
