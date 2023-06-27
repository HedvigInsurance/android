package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.claimtriaging.claimTriagingGraph
import com.hedvig.android.feature.home.claims.pledge.HonestyPledgeBottomSheet
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.home.legacychangeaddress.LegacyChangeAddressActivity
import com.hedvig.android.feature.legacyclaimtriaging.legacyClaimTriagingGraph
import com.hedvig.android.feature.odyssey.navigation.claimFlowGraph
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
import com.kiwi.navigationcompose.typed.popUpTo
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
          hedvigAppState.navController.navigate(destination, navOptions, navigatorExtras)
        }
      }

      override fun navigateUp() {
        hedvigAppState.navController.navigateUp()
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
      navController = hedvigAppState.navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      nestedGraphs = {
        changeAddressGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          density = density,
          navController = hedvigAppState.navController,
          openChat = context::startChat,
        )
        generateTravelCertificateGraph(
          density = density,
          navController = hedvigAppState.navController,
          applicationId = BuildConfig.APPLICATION_ID,
        )
        claimTriagingGraph(
          navigator = navigator,
          startClaimFlow = { backStackEntry, entryPointId, entryPointOptionId ->
            with(navigator) {
              backStackEntry.navigate(
                destination = AppDestination.ClaimsFlow(entryPointId, entryPointOptionId),
                navOptions = navOptions {
                  popUpTo<AppDestination.LegacyClaimsTriaging> {
                    inclusive = true
                  }
                },
              )
            }
          },
        )
        legacyClaimTriagingGraph(
          startClaimFlow = { backStackEntry, entryPointId ->
            with(navigator) {
              backStackEntry.navigate(
                destination = AppDestination.ClaimsFlow(entryPointId, null),
                navOptions = navOptions {
                  popUpTo<AppDestination.LegacyClaimsTriaging> {
                    inclusive = true
                  }
                },
              )
            }
          },
          navigateUp = navigator::navigateUp,
        )
        claimFlowGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navController = hedvigAppState.navController,
          navigator = navigator,
          imageLoader = imageLoader,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openAppSettings = {
            activityNavigator.openAppSettings(context)
          },
          openPlayStore = {
            activityNavigator.tryOpenPlayStore(context)
          },
          openChat = {
            hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
            activityNavigator.navigateToChat(context)
          },
          finishClaimFlow = {
            hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
          },
        )
      },
      onStartChat = { context.startChat() },
      onStartClaim = { backStackEntry ->
        coroutineScope.launch {
          hAnalytics.beginClaim(AppScreen.HOME)
          if (featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)) {
            with(navigator) {
              if (featureManager.isFeatureEnabled(Feature.CLAIMS_TRIAGING)) {
                backStackEntry.navigate(AppDestination.ClaimsTriaging)
              } else {
                backStackEntry.navigate(AppDestination.LegacyClaimsTriaging)
              }
            }
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
      startMovingFlow = {
        coroutineScope.launch {
          if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
            hedvigAppState.navController.navigate(AppDestination.ChangeAddress)
          } else {
            context.startActivity(
              LegacyChangeAddressActivity.newInstance(context),
            )
          }
        }
      },
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
    )
  }
}
