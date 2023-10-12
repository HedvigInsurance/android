package com.hedvig.android.app.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import coil.ImageLoader
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.chat.navigation.chatGraph
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import com.hedvig.android.feature.claimtriaging.claimTriagingDestinations
import com.hedvig.android.feature.forever.navigation.foreverGraph
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeBottomSheet
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressActivity
import com.hedvig.android.feature.insurances.insurance.insuranceGraph
import com.hedvig.android.feature.odyssey.navigation.claimFlowGraph
import com.hedvig.android.feature.odyssey.navigation.navigateToClaimFlowDestination
import com.hedvig.android.feature.odyssey.navigation.terminalClaimFlowStepDestinations
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.generateTravelCertificateGraph
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.BuildConfig
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo
import kotlinx.coroutines.launch

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  imageLoader: ImageLoader,
  market: Market,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()
  val navigator: Navigator = rememberNavigator(hedvigAppState.navController)

  fun startMovingFlow() {
    coroutineScope.launch {
      if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
        hedvigAppState.navController.navigate(AppDestination.ChangeAddress)
      } else {
        context.startActivity(
          LegacyChangeAddressActivity.newInstance(context),
        )
      }
    }
  }

  fun navigateToPayinScreen() {
    coroutineScope.launch {
      context.startActivity(
        connectPayinIntent(
          context,
          featureManager.getPaymentType(),
          market,
          false,
        ),
      )
    }
  }

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
          context = context,
          navigator = navigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          activityNavigator = activityNavigator,
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
        coroutineScope.launch {
          hAnalytics.beginClaim(AppScreen.HOME)
          val useNonEmbarkClaimsFlow = featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)
          val useNewClaimTriaging = featureManager.isFeatureEnabled(Feature.CLAIMS_TRIAGING)
          // Legacy triage was killed, so if we turn off new triage, we turn off the entire odyssey claim flow
          if (useNonEmbarkClaimsFlow && useNewClaimTriaging) {
            with(navigator) { backStackEntry.navigate(AppDestination.ClaimsFlow) }
          } else {
            HonestyPledgeBottomSheet
              .newInstance(
                embarkClaimsFlowIntent = EmbarkActivity.newInstance(
                  context = context,
                  storyName = "claims",
                  storyTitle = context.getString(
                    hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL,
                  ),
                ),
              )
              .show(fragmentManager, HonestyPledgeBottomSheet.TAG)
          }
        }
      },
      startMovingFlow = ::startMovingFlow,
      onGenerateTravelCertificateClicked = {
        hedvigAppState.navController.navigate(AppDestination.GenerateTravelCertificate)
      },
      navigateToPayinScreen = ::navigateToPayinScreen,
      openAppSettings = { activityNavigator.openAppSettings(context) },
      openUrl = ::openUrl,
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
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
      openChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat)
        }
      },
      startMovingFlow = ::startMovingFlow,
      startTerminationFlow = { backStackEntry: NavBackStackEntry, insuranceId: String, insuranceDisplayName: String ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.TerminateInsurance(insuranceId, insuranceDisplayName))
        }
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      imageLoader = imageLoader,
    )
    foreverGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    profileGraph(
      nestedGraphs = {},
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToPayoutScreen = navigateToPayoutScreen@{
        val intent = AdyenConnectPayoutActivity.newInstance(context, AdyenCurrency.fromMarket(market))
        context.startActivity(intent)
      },
      navigateToPayinScreen = ::navigateToPayinScreen,
      openAppSettings = { activityNavigator.openAppSettings(context) },
      openUrl = ::openUrl,
      market = market,
    )
    chatGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigator = navigator,
    )
  }
}

private fun NavGraphBuilder.nestedHomeGraphs(
  density: Density,
  hedvigAppState: HedvigAppState,
  context: Context,
  navigator: Navigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  activityNavigator: ActivityNavigator,
) {
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
    navigateToTriaging = { backStackEntry ->
      if (backStackEntry != null) {
        with(navigator) { backStackEntry.navigate(ClaimTriagingDestination.ClaimGroups) }
      } else {
        navigator.navigateUnsafe(ClaimTriagingDestination.ClaimGroups)
      }
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
