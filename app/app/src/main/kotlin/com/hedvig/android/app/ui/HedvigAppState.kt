package com.hedvig.android.app.ui

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.d
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun rememberHedvigAppState(
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberAnimatedNavController(),
): HedvigAppState {
  NavigationTrackingSideEffect(navController)
  TopLevelDestinationNavigationSideEffect(navController, hAnalytics, tabNotificationBadgeService, coroutineScope)
  return remember(
    navController,
    coroutineScope,
    tabNotificationBadgeService,
    featureManager,
    windowSizeClass,
  ) {
    HedvigAppState(
      navController,
      windowSizeClass,
      coroutineScope,
      tabNotificationBadgeService,
      featureManager,
    )
  }
}

@Stable
internal class HedvigAppState(
  val navController: NavHostController,
  val windowSizeClass: WindowSizeClass,
  private val coroutineScope: CoroutineScope,
  private val tabNotificationBadgeService: TabNotificationBadgeService,
  private val featureManager: FeatureManager,
) {
  val currentDestination: NavDestination?
    @Composable get() = navController.currentBackStackEntryAsState().value?.destination

  private val currentTopLevelAppDestination: AppDestination.TopLevelDestination?
    @Composable get() = currentDestination?.toTopLevelAppDestination()

  private val shouldShowNavBars: Boolean
    @Composable get() {
      if (currentTopLevelAppDestination != null) return true
      return currentDestination.isInListOfNonTopLevelNavBarPermittedDestinations()
    }

  val shouldShowBottomBar: Boolean
    @Composable
    get() {
      val bottomBarWidthRequirements = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
      return bottomBarWidthRequirements && shouldShowNavBars
    }

  val shouldShowNavRail: Boolean
    @Composable
    get() {
      val navRailWidthRequirements = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
      return navRailWidthRequirements && shouldShowNavBars
    }

  val backgroundColors: GradientColors
    @Composable
    get() {
      val isLightMode = !isSystemInDarkTheme()
      return when (currentTopLevelAppDestination) {
        AppDestination.TopLevelDestination.Home -> {
          if (isLightMode) {
            GradientColors(Color(0xFFC0CAD8), Color(0xFFEDCDAB), Color(0xFFF6F6F6))
          } else {
            GradientColors(Color(0xFF121212), Color(0xFF1B2631), Color(0xFF34221E))
          }
        }
        AppDestination.TopLevelDestination.Insurance -> {
          if (isLightMode) {
            GradientColors(Color(0xFFF6F6F6))
          } else {
            GradientColors(Color(0xFF121212))
          }
        }
        AppDestination.TopLevelDestination.Referrals -> {
          if (isLightMode) {
            GradientColors(Color(0xFFD3D3D3), Color(0xFFE5E5E5), Color(0xFFF6F6F6))
          } else {
            GradientColors(Color(0xFF121212), Color(0xFF131313), Color(0xFF262626))
          }
        }
        AppDestination.TopLevelDestination.Profile -> {
          if (isLightMode) {
            GradientColors(Color(0xFFDCDEF5), Color(0xFFEFF0FB), Color(0xFFF6F6F6))
          } else {
            GradientColors(Color(0xFF121212), Color(0xFF0F0F05), Color(0xFF1E1D0A))
          }
        }
        null -> GradientColors(Color.Transparent)
      }
    }

  val topLevelGraphs: StateFlow<ImmutableSet<TopLevelGraph>> = flow {
    val isReferralsEnabled = featureManager.isFeatureEnabled(Feature.REFERRALS)
    emit(
      listOfNotNull(
        TopLevelGraph.HOME,
        TopLevelGraph.INSURANCE,
        TopLevelGraph.REFERRALS.takeIf { isReferralsEnabled },
        TopLevelGraph.PROFILE,
      ).toPersistentSet(),
    )
  }.stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    persistentSetOf(
      TopLevelGraph.HOME,
      TopLevelGraph.INSURANCE,
      TopLevelGraph.PROFILE,
    ),
  )

  val topLevelGraphsWithNotifications: StateFlow<PersistentSet<TopLevelGraph>> =
    tabNotificationBadgeService.unseenTabNotificationBadges().map { bottomNavTabs: Set<BottomNavTab> ->
      bottomNavTabs.map(BottomNavTab::topTopLevelGraph).toPersistentSet()
    }.stateIn(
      coroutineScope,
      SharingStarted.WhileSubscribed(5.seconds),
      initialValue = persistentSetOf(),
    )

  /**
   * UI logic for navigating to a top level destination in the app. Top level destinations have
   * only one copy of the destination of the back stack, and save and restore state whenever you
   * navigate to and from it.
   *
   * @param topLevelGraph: The destination the app needs to navigate to.
   */
  fun navigateToTopLevelGraph(topLevelGraph: TopLevelGraph) {
    val topLevelNavOptions = navOptions {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
    when (topLevelGraph) {
      TopLevelGraph.HOME -> navController.navigate(TopLevelGraph.HOME, topLevelNavOptions)
      TopLevelGraph.INSURANCE -> navController.navigate(TopLevelGraph.INSURANCE, topLevelNavOptions)
      TopLevelGraph.PROFILE -> navController.navigate(TopLevelGraph.PROFILE, topLevelNavOptions)
      TopLevelGraph.REFERRALS -> navController.navigate(TopLevelGraph.REFERRALS, topLevelNavOptions)
    }
  }
}

@Composable
private fun NavigationTrackingSideEffect(navController: NavController) {
  DisposableEffect(navController) {
    val listener = NavController.OnDestinationChangedListener { _, destination: NavDestination, bundle: Bundle? ->
      d {
        buildString {
          append("Navigated to route:${destination.route}")
          if (bundle != null) {
            append(" | ")
            append("With bundle:$bundle")
          }
        }
      }
    }
    navController.addOnDestinationChangedListener(listener)
    onDispose {
      navController.removeOnDestinationChangedListener(listener)
    }
  }
}

@Composable
private fun TopLevelDestinationNavigationSideEffect(
  navController: NavController,
  hAnalytics: HAnalytics,
  tabNotificationBadgeService: TabNotificationBadgeService,
  coroutineScope: CoroutineScope,
) {
  DisposableEffect(navController, hAnalytics, tabNotificationBadgeService, coroutineScope) {
    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
      val topLevelDestination = destination.toTopLevelAppDestination() ?: return@OnDestinationChangedListener
      coroutineScope.launch {
        when (topLevelDestination) {
          AppDestination.TopLevelDestination.Home -> {
            hAnalytics.screenView(AppScreen.HOME)
            tabNotificationBadgeService.visitTab(BottomNavTab.HOME)
          }
          AppDestination.TopLevelDestination.Insurance -> {
            hAnalytics.screenView(AppScreen.INSURANCES)
            tabNotificationBadgeService.visitTab(BottomNavTab.INSURANCE)
          }
          AppDestination.TopLevelDestination.Referrals -> {
            hAnalytics.screenView(AppScreen.FOREVER)
            tabNotificationBadgeService.visitTab(BottomNavTab.REFERRALS)
          }
          AppDestination.TopLevelDestination.Profile -> {
            hAnalytics.screenView(AppScreen.PROFILE)
            tabNotificationBadgeService.visitTab(BottomNavTab.PROFILE)
          }
        }
      }
    }
    navController.addOnDestinationChangedListener(listener)
    onDispose {
      navController.removeOnDestinationChangedListener(listener)
    }
  }
}

@Immutable
internal data class GradientColors(
  val color1: Color,
  val color2: Color,
  val color3: Color,
) {
  constructor(color: Color) : this(color, color, color)
}

private fun BottomNavTab.topTopLevelGraph(): TopLevelGraph {
  return when (this) {
    BottomNavTab.HOME -> TopLevelGraph.HOME
    BottomNavTab.INSURANCE -> TopLevelGraph.INSURANCE
    BottomNavTab.REFERRALS -> TopLevelGraph.REFERRALS
    BottomNavTab.PROFILE -> TopLevelGraph.PROFILE
  }
}

private fun NavDestination?.toTopLevelAppDestination(): AppDestination.TopLevelDestination? {
  return when (this?.route) {
    createRoutePattern<AppDestination.TopLevelDestination.Home>() -> AppDestination.TopLevelDestination.Home
    createRoutePattern<AppDestination.TopLevelDestination.Insurance>() -> AppDestination.TopLevelDestination.Insurance
    createRoutePattern<AppDestination.TopLevelDestination.Referrals>() -> AppDestination.TopLevelDestination.Referrals
    createRoutePattern<AppDestination.TopLevelDestination.Profile>() -> AppDestination.TopLevelDestination.Profile
    else -> null
  }
}

/**
 * Special routes, which despite not being top level should still show the navigation bars.
 */
private fun NavDestination?.isInListOfNonTopLevelNavBarPermittedDestinations(): Boolean {
  return when (this?.route) {
    createRoutePattern<AppDestination.BusinessModel>() -> true
    createRoutePattern<AppDestination.Eurobonus>() -> true
    else -> false
  }
}
