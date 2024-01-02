package com.hedvig.android.app.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import coil.ImageLoader
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.chat.navigation.chatGraph
import com.hedvig.android.feature.claim.details.claimDetailsGraph
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsDestination
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import com.hedvig.android.feature.claimtriaging.claimTriagingDestinations
import com.hedvig.android.feature.connect.payment.adyen.connectAdyenPaymentGraph
import com.hedvig.android.feature.connect.payment.connectPaymentGraph
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredGraph
import com.hedvig.android.feature.forever.navigation.foreverGraph
import com.hedvig.android.feature.help.center.helpCenterGraph
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.insurances.insurance.insuranceGraph
import com.hedvig.android.feature.odyssey.navigation.claimFlowGraph
import com.hedvig.android.feature.odyssey.navigation.navigateToClaimFlowDestination
import com.hedvig.android.feature.odyssey.navigation.terminalClaimFlowStepDestinations
import com.hedvig.android.feature.payments.navigation.paymentsGraph
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.generateTravelCertificateGraph
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.BuildConfig
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  navigateToConnectPayment: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  imageLoader: ImageLoader,
  market: Market,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val density = LocalDensity.current
  val navigator: Navigator = rememberNavigator(hedvigAppState.navController)

  fun openUrl(url: String) {
    activityNavigator.openWebsite(
      context,
      if (url.isBlank()) Uri.EMPTY else Uri.parse(url),
    )
  }
  NavHost(
    navController = hedvigAppState.navController,
    startDestination = createRoutePattern<TopLevelGraph.HOME>(),
    route = "root",
    modifier = modifier,
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    homeGraph(
      nestedGraphs = {
        nestedHomeGraphs(
          density = density,
          hedvigAppState = hedvigAppState,
          hedvigBuildConstants = hedvigBuildConstants,
          context = context,
          navigator = navigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          activityNavigator = activityNavigator,
          imageLoader = imageLoader,
          openUrl = ::openUrl,
        )
      },
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onStartChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat)
        }
      },
      onStartClaim = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(AppDestination.ClaimsFlow) }
      },
      startMovingFlow = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.ChangeAddress)
        }
      },
      onGenerateTravelCertificateClicked = {
        hedvigAppState.navController.navigate(AppDestination.GenerateTravelCertificate)
      },
      navigateToClaimDetails = { backStackEntry, claimId ->
        with(navigator) { backStackEntry.navigate(ClaimDetailsDestination(claimId)) }
      },
      navigateToPayinScreen = navigateToConnectPayment,
      navigateToMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) { backStackEntry.navigate(EditCoInsuredDestination.AddInfo(contractId)) }
      },
      navigateToHelpCenter = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(HelpCenterDestination) }
      },
      openAppSettings = { activityNavigator.openAppSettings(context) },
      openUrl = ::openUrl,
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navigator = navigator,
          navController = hedvigAppState.navController,
          openChat = { backStackEntry ->
            with(navigator) {
              backStackEntry.navigate(AppDestination.Chat)
            }
          },
          openPlayStore = { activityNavigator.tryOpenPlayStore(context) },
        )
      },
      navigator = navigator,
      openWebsite = { uri ->
        activityNavigator.openWebsite(context, uri)
      },
      openUrl = {
        openUrl(it)
      },
      openChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat)
        }
      },
      startMovingFlow = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.ChangeAddress)
        }
      },
      startTerminationFlow = { backStackEntry: NavBackStackEntry, insuranceId: String, insuranceDisplayName: String ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.TerminateInsurance(insuranceId, insuranceDisplayName))
        }
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      imageLoader = imageLoader,
      startEditCoInsured = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(EditCoInsuredDestination.AddOrRemove(contractId))
        }
      },
      startEditCoInsuredAddMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(EditCoInsuredDestination.AddInfo(contractId))
        }
      },
    )
    foreverGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    profileGraph(
      nestedGraphs = {
        paymentsGraph(
          navigator = navigator,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          navigateToConnectPayment = navigateToConnectPayment,
        )
      },
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToPaymentInfo = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(AppDestination.PaymentInfo) }
      },
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToAddMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(EditCoInsuredDestination.AddInfo(contractId))
        }
      },
      openAppSettings = { activityNavigator.openAppSettings(context) },
      navigateToHelpCenter = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(HelpCenterDestination) }
      },
      openUrl = ::openUrl,
    )
    chatGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      openUrl = ::openUrl,
      navigator = navigator,
    )
    connectPaymentGraph(
      navigator = navigator,
      market = market,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigateToAdyenConnectPayment = {
        navigator.navigateUnsafe(
          AppDestination.ConnectPaymentAdyen,
          navOptions {
            popUpTo(createRoutePattern<AppDestination.ConnectPayment>()) {
              inclusive = true
            }
          },
        )
      },
    )
    editCoInsuredGraph(
      navigateUp = navigator::navigateUp,
      navController = hedvigAppState.navController,
    )
    connectAdyenPaymentGraph(navigator)
    helpCenterGraph(
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      openChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat)
        }
      },
    )
  }
}

private fun NavGraphBuilder.nestedHomeGraphs(
  density: Density,
  hedvigAppState: HedvigAppState,
  hedvigBuildConstants: HedvigBuildConstants,
  context: Context,
  navigator: Navigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  activityNavigator: ActivityNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  claimDetailsGraph(
    imageLoader = imageLoader,
    openUrl = openUrl,
    navigateUp = navigator::navigateUp,
    appPackageId = hedvigBuildConstants.appId,
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat)
      }
    },
  )
  changeAddressGraph(
    navController = hedvigAppState.navController,
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat)
      }
    },
    openUrl = { activityNavigator.openWebsite(context, Uri.parse(it)) },
  )
  generateTravelCertificateGraph(
    density = density,
    navController = hedvigAppState.navController,
    applicationId = BuildConfig.APPLICATION_ID,
  )
  claimFlowGraph(
    windowSizeClass = hedvigAppState.windowSizeClass,
    navigator = navigator,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    navigateToTriaging = {
      navigator.navigateUnsafe(ClaimTriagingDestination.ClaimGroups)
    },
    openAppSettings = {
      activityNavigator.openAppSettings(context)
    },
    closeClaimFlow = {
      hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
    },
    nestedGraphs = {
      claimTriagingDestinations(
        navigator = navigator,
        startClaimFlow = { backStackEntry, claimFlowStep: ClaimFlowStep ->
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        closeClaimFlow = {
          hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
        },
      )
    },
    openUrl = { activityNavigator.openWebsite(context, Uri.parse(it)) },
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat)
      }
    },
    imageLoader = imageLoader,
  )
  terminalClaimFlowStepDestinations(
    navigator = navigator,
    openPlayStore = {
      navigator.popBackStack()
      activityNavigator.tryOpenPlayStore(context)
    },
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(destination = AppDestination.Chat) {
          popUpTo<AppDestination.TopLevelDestination.Home>()
        }
      }
    },
  )
}

@Composable
private fun rememberNavigator(navController: NavController): Navigator {
  return remember(navController) {
    object : Navigator {
      override fun NavBackStackEntry.navigate(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
          navigateUnsafe(destination, navOptions, navigatorExtras)
        }
      }

      override fun navigateUnsafe(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        navController.navigate(destination, navOptions, navigatorExtras)
      }

      override fun navigateUp() {
        navController.navigateUp()
      }

      override fun popBackStack() {
        navController.popBackStack()
      }
    }
  }
}
