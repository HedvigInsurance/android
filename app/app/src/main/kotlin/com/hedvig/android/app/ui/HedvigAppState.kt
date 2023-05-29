package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
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
fun rememberHedvigAppState(
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberAnimatedNavController(),
): HedvigAppState {
  NavigationTrackingSideEffect(navController)
  NotificationBadgeVisitSideEffect(navController, hAnalytics, tabNotificationBadgeService, coroutineScope)
  return remember(
    navController,
    coroutineScope,
    tabNotificationBadgeService,
    featureManager,
    windowSizeClass,
  ) {
    HedvigAppState(
      navController,
      coroutineScope,
      tabNotificationBadgeService,
      featureManager,
      windowSizeClass,
    )
  }
}

@Stable
class HedvigAppState(
  val navController: NavHostController,
  private val coroutineScope: CoroutineScope,
  private val tabNotificationBadgeService: TabNotificationBadgeService,
  private val featureManager: FeatureManager,
  private val windowSizeClass: WindowSizeClass,
) {
  val currentDestination: NavDestination?
    @Composable get() = navController.currentBackStackEntryAsState().value?.destination

  val currentTopLevelDestination: TopLevelDestination?
    @Composable get() = currentDestination?.toTopLevelDestination()

  val shouldShowBottomBar: Boolean
    @Composable
    get() {
      val bottomBarWidthRequirements = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
      return bottomBarWidthRequirements && currentTopLevelDestination != null
    }

  val shouldShowNavRail: Boolean
    @Composable
    get() {
      val navRailWidthRequirements = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
      return navRailWidthRequirements && currentTopLevelDestination != null
    }

  val backgroundColors: NonEmptyList<Color>?
    @Composable
    get() {
      val isLightMode = !isSystemInDarkTheme()
      return when (currentTopLevelDestination) {
        TopLevelDestination.HOME -> {
          if (isLightMode) {
            listOf(Color(0xFFC0CAD8), Color(0xFFEDCDAB), Color(0xFFF6F6F6))
          } else {
            listOf(Color(0xFF121212), Color(0xFF1B2631), Color(0xFF34221E))
          }
        }
        TopLevelDestination.INSURANCE -> {
          if (isLightMode) {
            listOf(Color(0xFFF6F6F6))
          } else {
            listOf(Color(0xFF121212))
          }
        }
        TopLevelDestination.REFERRALS -> {
          if (isLightMode) {
            listOf(Color(0xFFD3D3D3), Color(0xFFE5E5E5), Color(0xFFF6F6F6))
          } else {
            listOf(Color(0xFF121212), Color(0xFF131313), Color(0xFF262626))
          }
        }
        TopLevelDestination.PROFILE -> {
          if (isLightMode) {
            listOf(Color(0xFFDCDEF5), Color(0xFFEFF0FB), Color(0xFFF6F6F6))
          } else {
            listOf(Color(0xFF121212), Color(0xFF0F0F05), Color(0xFF1E1D0A))
          }
        }
        null -> null
      }?.toNonEmptyListOrNull()
    }

  val topLevelDestinations: StateFlow<ImmutableSet<TopLevelDestination>> = flow {
    val isReferralsEnabled = featureManager.isFeatureEnabled(Feature.REFERRALS)
    emit(
      listOfNotNull(
        TopLevelDestination.HOME,
        TopLevelDestination.INSURANCE,
        TopLevelDestination.REFERRALS.takeIf { isReferralsEnabled },
        TopLevelDestination.PROFILE,
      ).toPersistentSet(),
    )
  }.stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    persistentSetOf(
      TopLevelDestination.HOME,
      TopLevelDestination.INSURANCE,
      TopLevelDestination.PROFILE,
    ),
  )

  val topLevelDestinationsWithNotifications: StateFlow<PersistentSet<TopLevelDestination>> =
    tabNotificationBadgeService.unseenTabNotificationBadges().map { bottomNavTabs: Set<BottomNavTab> ->
      bottomNavTabs.map(BottomNavTab::toLoggedInTab).toPersistentSet()
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
   * @param topLevelDestination: The destination the app needs to navigate to.
   */
  fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
    val topLevelNavOptions = navOptions {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
    when (topLevelDestination) {
      TopLevelDestination.HOME -> navController.navigate(TopLevelDestination.HOME, topLevelNavOptions)
      TopLevelDestination.INSURANCE -> navController.navigate(TopLevelDestination.INSURANCE, topLevelNavOptions)
      TopLevelDestination.PROFILE -> navController.navigate(TopLevelDestination.PROFILE, topLevelNavOptions)
      TopLevelDestination.REFERRALS -> navController.navigate(TopLevelDestination.REFERRALS, topLevelNavOptions)
    }
  }
}

@Composable
private fun NavigationTrackingSideEffect(navController: NavHostController) {
  val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
    d { "Navigated to route:${destination.route}" }
  }

  DisposableEffect(navController) {
    navController.addOnDestinationChangedListener(listener)
    onDispose {
      navController.removeOnDestinationChangedListener(listener)
    }
  }
}

@Composable
private fun NotificationBadgeVisitSideEffect(
  navController: NavHostController,
  hAnalytics: HAnalytics,
  tabNotificationBadgeService: TabNotificationBadgeService,
  coroutineScope: CoroutineScope,
) {
  val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
    val topLevelDestination = destination.toTopLevelDestination()
    when (topLevelDestination) {
      TopLevelDestination.HOME -> hAnalytics.screenView(AppScreen.HOME)
      TopLevelDestination.INSURANCE -> hAnalytics.screenView(AppScreen.INSURANCES)
      TopLevelDestination.REFERRALS -> hAnalytics.screenView(AppScreen.FOREVER)
      TopLevelDestination.PROFILE -> hAnalytics.screenView(AppScreen.PROFILE)
      null -> {}
    }
    coroutineScope.launch {
      topLevelDestination?.let { destination ->
        tabNotificationBadgeService.visitTab(destination.toBottomNavTab())
      }
    }
  }

  DisposableEffect(navController) {
    navController.addOnDestinationChangedListener(listener)
    onDispose {
      navController.removeOnDestinationChangedListener(listener)
    }
  }
}

private fun BottomNavTab.toLoggedInTab(): TopLevelDestination {
  return when (this) {
    BottomNavTab.HOME -> TopLevelDestination.HOME
    BottomNavTab.INSURANCE -> TopLevelDestination.INSURANCE
    BottomNavTab.REFERRALS -> TopLevelDestination.REFERRALS
    BottomNavTab.PROFILE -> TopLevelDestination.PROFILE
  }
}

private fun TopLevelDestination.toBottomNavTab(): BottomNavTab {
  return when (this) {
    TopLevelDestination.HOME -> BottomNavTab.HOME
    TopLevelDestination.INSURANCE -> BottomNavTab.INSURANCE
    TopLevelDestination.REFERRALS -> BottomNavTab.REFERRALS
    TopLevelDestination.PROFILE -> BottomNavTab.PROFILE
  }
}

// Turns a NavDestination into a TopLevelDestination if it matches one.
private fun NavDestination?.toTopLevelDestination(): TopLevelDestination? {
  return when (this?.route) {
    createRoutePattern<TopLevelDestination.HOME>() -> TopLevelDestination.HOME
    createRoutePattern<TopLevelDestination.INSURANCE>() -> TopLevelDestination.INSURANCE
    createRoutePattern<TopLevelDestination.REFERRALS>() -> TopLevelDestination.REFERRALS
    createRoutePattern<TopLevelDestination.PROFILE>() -> TopLevelDestination.PROFILE
    else -> null
  }
}
