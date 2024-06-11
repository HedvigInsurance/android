package com.hedvig.android.app.ui

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
import androidx.navigation.navOptions
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.hedvig.android.app.navigation.RootGraph
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.insurancesBottomNavPermittedDestinations
import com.hedvig.android.feature.login.navigation.LoginDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.profileBottomNavPermittedDestinations
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popUpTo
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalTrackingApi::class)
@Composable
internal fun rememberHedvigAppState(
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  navHostController: NavHostController,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): HedvigAppState {
  NavigationViewTrackingEffect(navController = navHostController)
  TopLevelDestinationNavigationSideEffect(navHostController, tabNotificationBadgeService, coroutineScope)
  return remember(
    navHostController,
    windowSizeClass,
    coroutineScope,
    tabNotificationBadgeService,
    settingsDataStore,
    getOnlyHasNonPayingContractsUseCase,
    featureManager,
  ) {
    HedvigAppState(
      navController = navHostController,
      windowSizeClass = windowSizeClass,
      coroutineScope = coroutineScope,
      tabNotificationBadgeService = tabNotificationBadgeService,
      settingsDataStore = settingsDataStore,
      getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
      featureManager = featureManager,
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
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
) {
  val currentDestination: NavDestination?
    @Composable get() = navController.currentBackStackEntryAsState().value?.destination

  private val shouldShowNavBars: Boolean
    @Composable get() {
      if (currentDestination?.toTopLevelAppDestination() != null) return true
      return currentDestination.isInListOfNonTopLevelNavBarPermittedDestinations()
    }

  val navigationSuiteType: NavigationSuiteType
    @Composable
    get() {
      if (!shouldShowNavBars) return NavigationSuiteType.None
      val bottomBarWidthRequirements = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
      return if (bottomBarWidthRequirements) {
        NavigationSuiteType.NavigationBar
      } else {
        NavigationSuiteType.NavigationRail
      }
    }

  /**
   * App kill-switch. If this is enabled we must show nothing in the app but a button to try to update the app
   */
  val mustForceUpdate: StateFlow<Boolean> = featureManager
    .isFeatureEnabled(Feature.UPDATE_NECESSARY)
    .stateIn(
      coroutineScope,
      SharingStarted.WhileSubscribed(5.seconds),
      false,
    )

  val topLevelGraphs: StateFlow<Set<TopLevelGraph>> = flow {
    val onlyHasNonPayingContracts = getOnlyHasNonPayingContractsUseCase.provide().invoke().getOrNull()
    emit(
      buildList {
        add(TopLevelGraph.Home)
        add(TopLevelGraph.Insurances)
        if (onlyHasNonPayingContracts != true) {
          add(TopLevelGraph.Forever)
        }
        add(TopLevelGraph.Payments)
        add(TopLevelGraph.Profile)
      }.toSet(),
    )
  }.stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    setOf(
      TopLevelGraph.Home,
      TopLevelGraph.Insurances,
      TopLevelGraph.Payments,
      TopLevelGraph.Profile,
    ),
  )

  val topLevelGraphsWithNotifications: StateFlow<Set<TopLevelGraph>> =
    tabNotificationBadgeService.unseenTabNotificationBadges().map { bottomNavTabs: Set<BottomNavTab> ->
      bottomNavTabs.map { bottomNavTab ->
        when (bottomNavTab) {
          BottomNavTab.HOME -> TopLevelGraph.Home
          BottomNavTab.INSURANCE -> TopLevelGraph.Insurances
          BottomNavTab.FOREVER -> TopLevelGraph.Forever
          BottomNavTab.PAYMENTS -> TopLevelGraph.Payments
          BottomNavTab.PROFILE -> TopLevelGraph.Profile
        }
      }.toSet()
    }.stateIn(
      scope = coroutineScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = setOf(),
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
      TopLevelGraph.Home -> navController.navigate(HomeDestination.Graph, topLevelNavOptions)
      TopLevelGraph.Insurances -> navController.navigate(InsurancesDestination.Graph, topLevelNavOptions)
      TopLevelGraph.Forever -> navController.navigate(ForeverDestination.Graph, topLevelNavOptions)
      TopLevelGraph.Payments -> navController.navigate(PaymentsDestination.Graph, topLevelNavOptions)
      TopLevelGraph.Profile -> navController.navigate(ProfileDestination.Graph, topLevelNavOptions)
    }
  }

  /**
   * These should also save/restore state when it's possible to do so.
   * Should also try to find a way to *not* save the state when explicitly logging out. The backstack saving should
   * only happen in scenarios where the logout was due to token expiration. The most common scenario there would be
   * when coming into the app from a deep link while not having valid cretentials lying around already.
   * https://issuetracker.google.com/issues/334413738
   */
  fun navigateToLoggedIn() {
    navController.navigate(RootGraph.route) {
      popUpTo<LoginDestination> {
        inclusive = true
      }
    }
  }

  fun navigateToLoggedOut() {
    navController.navigate(LoginDestination) {
      popUpTo(RootGraph.route) {
        inclusive = true
      }
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

@JvmInline
value class NavigationSuiteType private constructor(private val description: String) {
  override fun toString(): String = description

  companion object {
    val NavigationBar = NavigationSuiteType(description = "NavigationBar")
    val NavigationRail = NavigationSuiteType(description = "NavigationRail")
    val None = NavigationSuiteType(description = "None")
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
      when (topLevelDestination) {
        TopLevelDestination.Home -> {
          logcat { "Navigated to top level screen: HOME" }
          coroutineScope.launch { tabNotificationBadgeService.visitTab(BottomNavTab.HOME) }
        }

        TopLevelDestination.Insurances -> {
          logcat { "Navigated to top level screen: INSURANCES" }
          coroutineScope.launch { tabNotificationBadgeService.visitTab(BottomNavTab.INSURANCE) }
        }

        TopLevelDestination.Forever -> {
          logcat { "Navigated to top level screen: FOREVER" }
          coroutineScope.launch { tabNotificationBadgeService.visitTab(BottomNavTab.FOREVER) }
        }

        TopLevelDestination.Payments -> {
          logcat { "Navigated to top level screen: PAYMENTS" }
          coroutineScope.launch { tabNotificationBadgeService.visitTab(BottomNavTab.PAYMENTS) }
        }

        TopLevelDestination.Profile -> {
          logcat { "Navigated to top level screen: PROFILE" }
          coroutineScope.launch { tabNotificationBadgeService.visitTab(BottomNavTab.PROFILE) }
        }
      }
    }
    navController.addOnDestinationChangedListener(listener)
    onDispose {
      navController.removeOnDestinationChangedListener(listener)
    }
  }
}

private fun NavDestination?.toTopLevelAppDestination(): TopLevelDestination? {
  return when (this?.route) {
    createRoutePattern<HomeDestination.Home>() -> TopLevelDestination.Home
    createRoutePattern<InsurancesDestination.Insurances>() -> TopLevelDestination.Insurances
    createRoutePattern<ForeverDestination.Forever>() -> TopLevelDestination.Forever
    createRoutePattern<PaymentsDestination.Payments>() -> TopLevelDestination.Payments
    createRoutePattern<ProfileDestination.Profile>() -> TopLevelDestination.Profile
    else -> null
  }
}

private fun NavDestination?.isInListOfNonTopLevelNavBarPermittedDestinations(): Boolean {
  return this?.route in bottomNavPermittedDestinations
}

/**
 * Special routes, which despite not being top level should still show the navigation bars.
 */
private val bottomNavPermittedDestinations: List<String> = buildList {
  addAll(profileBottomNavPermittedDestinations)
  addAll(insurancesBottomNavPermittedDestinations)
}

private sealed interface TopLevelDestination {
  val destination: Destination

  object Home : TopLevelDestination {
    override val destination: Destination = HomeDestination.Home
  }

  object Insurances : TopLevelDestination {
    override val destination: Destination = InsurancesDestination.Insurances
  }

  object Forever : TopLevelDestination {
    override val destination: Destination = ForeverDestination.Forever
  }

  object Payments : TopLevelDestination {
    override val destination: Destination = PaymentsDestination.Payments
  }

  object Profile : TopLevelDestination {
    override val destination: Destination = ProfileDestination.Profile
  }
}
