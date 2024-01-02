package com.hedvig.android.app.ui

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.datadog.android.rum.GlobalRumMonitor
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.insurances.navigation.insurancesBottomNavPermittedDestinations
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import kotlin.time.Duration.Companion.seconds
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

@Composable
internal fun rememberHedvigAppState(
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  settingsDataStore: SettingsDataStore,
  featureManager: FeatureManager,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberNavController(),
): HedvigAppState {
  NavigationTrackingSideEffect(navController)
  TopLevelDestinationNavigationSideEffect(navController, tabNotificationBadgeService, coroutineScope)
  return remember(
    navController,
    coroutineScope,
    tabNotificationBadgeService,
    featureManager,
    settingsDataStore,
    windowSizeClass,
  ) {
    HedvigAppState(
      navController,
      windowSizeClass,
      coroutineScope,
      tabNotificationBadgeService,
      settingsDataStore,
      featureManager,
    )
  }
}

@Stable
internal class HedvigAppState(
  val navController: NavHostController,
  val windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope,
  tabNotificationBadgeService: TabNotificationBadgeService,
  private val settingsDataStore: SettingsDataStore,
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

  val topLevelGraphs: StateFlow<ImmutableSet<TopLevelGraph>> = flow {
    val isForeverEnabled = featureManager.isFeatureEnabled(Feature.FOREVER)
    emit(
      listOfNotNull(
        TopLevelGraph.HOME,
        TopLevelGraph.INSURANCE,
        TopLevelGraph.FOREVER.takeIf { isForeverEnabled },
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
      TopLevelGraph.FOREVER -> navController.navigate(TopLevelGraph.FOREVER, topLevelNavOptions)
    }
  }

  val darkTheme: Boolean
    @Composable
    get() {
      val selectedTheme by settingsDataStore.observeTheme().collectAsState(initial = null)
      return when (selectedTheme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> isSystemInDarkTheme()
      }
    }
}

@Composable
private fun NavigationTrackingSideEffect(navController: NavController) {
  DisposableEffect(navController) {
    val listener = NavController.OnDestinationChangedListener { _, destination: NavDestination, bundle: Bundle? ->
      logcat {
        buildString {
          append("Navigated to route:${destination.route}")
          if (bundle != null) {
            append(" | ")
            append("With bundle:$bundle")
          }
        }
      }
      GlobalRumMonitor.get().startView(
        key = destination,
        name = destination.route ?: "Unknown route",
      )
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
  tabNotificationBadgeService: TabNotificationBadgeService,
  coroutineScope: CoroutineScope,
) {
  DisposableEffect(navController, tabNotificationBadgeService, coroutineScope) {
    val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
      val topLevelDestination = destination.toTopLevelAppDestination() ?: return@OnDestinationChangedListener
      coroutineScope.launch {
        when (topLevelDestination) {
          AppDestination.TopLevelDestination.Home -> {
            logcat { "Navigated to top level screen: HOME" }
            tabNotificationBadgeService.visitTab(BottomNavTab.HOME)
          }
          AppDestination.TopLevelDestination.Insurance -> {
            logcat { "Navigated to top level screen: INSURANCES" }
            tabNotificationBadgeService.visitTab(BottomNavTab.INSURANCE)
          }
          AppDestination.TopLevelDestination.Forever -> {
            logcat { "Navigated to top level screen: FOREVER" }
            tabNotificationBadgeService.visitTab(BottomNavTab.REFERRALS)
          }
          AppDestination.TopLevelDestination.Profile -> {
            logcat { "Navigated to top level screen: PROFILE" }
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

private fun BottomNavTab.topTopLevelGraph(): TopLevelGraph {
  return when (this) {
    BottomNavTab.HOME -> TopLevelGraph.HOME
    BottomNavTab.INSURANCE -> TopLevelGraph.INSURANCE
    BottomNavTab.REFERRALS -> TopLevelGraph.FOREVER
    BottomNavTab.PROFILE -> TopLevelGraph.PROFILE
  }
}

private fun NavDestination?.toTopLevelAppDestination(): AppDestination.TopLevelDestination? {
  return when (this?.route) {
    createRoutePattern<AppDestination.TopLevelDestination.Home>() -> AppDestination.TopLevelDestination.Home
    createRoutePattern<AppDestination.TopLevelDestination.Insurance>() -> AppDestination.TopLevelDestination.Insurance
    createRoutePattern<AppDestination.TopLevelDestination.Forever>() -> AppDestination.TopLevelDestination.Forever
    createRoutePattern<AppDestination.TopLevelDestination.Profile>() -> AppDestination.TopLevelDestination.Profile
    else -> null
  }
}

/**
 * Special routes, which despite not being top level should still show the navigation bars.
 */
private val bottomNavPermittedDestinations: List<String> = buildList {
  add(createRoutePattern<AppDestination.Eurobonus>())
  addAll(insurancesBottomNavPermittedDestinations)
}

private fun NavDestination?.isInListOfNonTopLevelNavBarPermittedDestinations(): Boolean {
  return this?.route in bottomNavPermittedDestinations
}
