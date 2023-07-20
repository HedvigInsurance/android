package com.hedvig.android.app.navigation

import android.content.Context
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import com.hedvig.android.feature.claimtriaging.claimTriagingDestinations
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeBottomSheet
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressActivity
import com.hedvig.android.feature.odyssey.navigation.claimFlowGraph
import com.hedvig.android.feature.odyssey.navigation.navigateToClaimFlowDestination
import com.hedvig.android.feature.odyssey.navigation.terminalClaimFlowStepDestinations
import com.hedvig.android.feature.travelcertificate.navigation.generateTravelCertificateGraph
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.BuildConfig
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.home.ui.HowClaimsWorkDialog
import com.hedvig.app.feature.insurance.ui.detail.contractDetailGraph
import com.hedvig.app.feature.insurance.ui.tab.insuranceGraph
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.profile.ui.tab.profileGraph
import com.hedvig.app.feature.referrals.ui.tab.referralsGraph
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.startChat
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack
import kotlinx.coroutines.launch

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  imageLoader: ImageLoader,
  marketManager: MarketManager,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  isProduction: Boolean,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val resources = context.resources
  val density = LocalDensity.current
  val coroutineScope = rememberCoroutineScope()
  val navigator: Navigator = remember(hedvigAppState) {
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
        hedvigAppState.navController.navigate(destination, navOptions, navigatorExtras)
      }

      override fun navigateUp() {
        hedvigAppState.navController.navigateUp()
      }

      override fun popBackStack() {
        hedvigAppState.navController.popBackStack()
      }
    }
  }

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
  AnimatedNavHost(
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
      navController = hedvigAppState.navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onStartChat = { context.startChat() },
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
      startMovingFlow = { startMovingFlow() },
      onHowClaimsWorkClick = { howClaimsWorkList ->
        val howClaimsWorkData = howClaimsWorkList.mapIndexed { index, howClaimsWork ->
          DismissiblePagerModel.NoTitlePage(
            imageUrls = ThemedIconUrls.from(
              howClaimsWork.illustration.variants.fragments.iconVariantsFragment,
            ),
            paragraph = howClaimsWork.body,
            buttonText = resources.getString(
              if (index == howClaimsWorkList.lastIndex) {
                hedvig.resources.R.string.claims_explainer_button_start_claim
              } else {
                hedvig.resources.R.string.claims_explainer_button_next
              },
            ),
          )
        }
        HowClaimsWorkDialog
          .newInstance(howClaimsWorkData)
          .show(fragmentManager, HowClaimsWorkDialog.TAG)
      },
      onGenerateTravelCertificateClicked = {
        hedvigAppState.navController.navigate(AppDestination.GenerateTravelCertificate)
      },
      navigateToPayinScreen = navigateToPayinScreen@{ paymentType ->
        val market = marketManager.market ?: return@navigateToPayinScreen
        context.startActivity(
          connectPayinIntent(
            context,
            paymentType,
            market,
            false,
          ),
        )
      },
      tryOpenUri = { uri ->
        if (context.canOpenUri(uri)) {
          context.openUri(uri)
        }
      },
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
    )
    insuranceGraph(
      nestedGraphs = {
        contractDetailGraph(
          density = density,
          navigator = navigator,
          onEditCoInsuredClick = {
            activityNavigator.navigateToChat(context)
          },
          onChangeAddressClick = {
            startMovingFlow()
          },
          imageLoader = imageLoader,
        )
      },
      navigateToContractDetailScreen = { backStackEntry, contractId: String ->
        with(navigator) { backStackEntry.navigate(AppDestination.ContractDetail(contractId)) }
      },
      imageLoader = imageLoader,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    referralsGraph(
      languageService = languageService,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    profileGraph(
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      windowSizeClass = hedvigAppState.windowSizeClass,
      isProduction = isProduction,
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
    density = density,
    navController = hedvigAppState.navController,
    openChat = context::startChat,
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
    openChat = {
      navigator.popBackStack()
      activityNavigator.navigateToChat(context)
    },
  )
}
