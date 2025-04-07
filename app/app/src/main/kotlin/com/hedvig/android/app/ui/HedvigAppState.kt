package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
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
import com.hedvig.android.app.notification.senders.CurrentDestinationInMemoryStorage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.forever.navigation.ForeverDestination
import com.hedvig.android.feature.help.center.navigation.helpCenterCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.home.home.navigation.homeCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.insurancesBottomNavPermittedDestinations
import com.hedvig.android.feature.insurances.navigation.insurancesCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.login.navigation.LoginDestination
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.profileBottomNavPermittedDestinations
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.typedClearBackStack
import com.hedvig.android.navigation.compose.typedHasRoute
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import kotlin.reflect.KClass
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
  RegisterOnDestinationChangedListenerSideEffect(navHostController, tabNotificationBadgeService, coroutineScope)
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
      return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
        else -> {
          when (windowSizeClass.heightSizeClass) {
            WindowHeightSizeClass.Expanded -> NavigationSuiteType.NavigationRailXLarge
            else -> NavigationSuiteType.NavigationRail
          }
        }
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

  val isInScreenEligibleForCrossSells: Boolean
    @Composable
    get() {
      return currentDestination?.isInListOfScreensPermittingCrossSellsSheetToShow() == true
    }

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
    tabNotificationBadgeService
      .unseenTabNotificationBadges()
      .map { bottomNavTabs: Set<BottomNavTab> ->
        bottomNavTabs
          .map { bottomNavTab ->
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
    val popToStartOfGraph = navController.currentDestination?.isTopLevelGraphInHierarchy(topLevelGraph) == true
    if (popToStartOfGraph) {
      navController.typedPopBackStack(destination = topLevelGraph.startDestination, inclusive = false)
    } else {
      navController.navigate(
        route = topLevelGraph.destination,
        navOptions = navOptions {
          popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
          }
          launchSingleTop = true
          restoreState = true
        },
      )
    }
  }

  /**
   * These should also save/restore state when it's possible to do so.
   * Should also try to find a way to *not* save the state when explicitly logging out. The backstack saving should
   * only happen in scenarios where the logout was due to token expiration. The most common scenario there would be
   * when coming into the app from a deep link while not having valid cretentials lying around already.
   * https://issuetracker.google.com/issues/334413738
   * todo: Now that we clear the backstack of all graphs on logout manually perhaps we can make this work properly
   */
  fun navigateToLoggedIn() {
    navController.navigate(RootGraph) {
      typedPopUpTo<LoginDestination> {
        inclusive = true
      }
    }
  }

  fun navigateToLoggedOut() {
    for (entry in TopLevelGraph.entries) {
      navController.typedClearBackStack(entry.destination)
    }
    navController.navigate(LoginDestination) {
      typedPopUpTo<RootGraph> {
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
value class NavigationSuiteType private constructor(
  private val description: String,
) {
  override fun toString(): String = description

  companion object {
    val NavigationBar = NavigationSuiteType(description = "NavigationBar")
    val NavigationRail = NavigationSuiteType(description = "NavigationRail")
    val NavigationRailXLarge = NavigationSuiteType(description = "NavigationRailXL")
    val None = NavigationSuiteType(description = "None")
  }
}

@Composable
private fun RegisterOnDestinationChangedListenerSideEffect(
  navController: NavController,
  tabNotificationBadgeService: TabNotificationBadgeService,
  coroutineScope: CoroutineScope,
) {
  DisposableEffect(navController, tabNotificationBadgeService, coroutineScope) {
    val listener = NavController.OnDestinationChangedListener { _, destination, bundle ->
      logcat { "Navigated to route:${destination.route} | bundle:$bundle" }
      CurrentDestinationInMemoryStorage.currentDestination = destination
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
  return when {
    this == null -> null
    typedHasRoute<HomeDestination.Home>() -> TopLevelDestination.Home
    typedHasRoute<InsurancesDestination.Insurances>() -> TopLevelDestination.Insurances
    typedHasRoute<ForeverDestination.Forever>() -> TopLevelDestination.Forever
    typedHasRoute<PaymentsDestination.Payments>() -> TopLevelDestination.Payments
    typedHasRoute<ProfileDestination.Profile>() -> TopLevelDestination.Profile
    else -> null
  }
}

private fun NavDestination?.isInListOfNonTopLevelNavBarPermittedDestinations(): Boolean {
  return bottomNavPermittedDestinations.any { this?.typedHasRoute(it) == true }
}

private fun NavDestination?.isInListOfScreensPermittingCrossSellsSheetToShow(): Boolean {
  return crossSellBottomSheetPermittingDestinations.any { this?.typedHasRoute(it) == true }
}

/**
 * Special routes, which despite not being top level should still show the navigation bars.
 */
private val bottomNavPermittedDestinations: List<KClass<out Destination>> = buildList {
  addAll(profileBottomNavPermittedDestinations)
  addAll(insurancesBottomNavPermittedDestinations)
}

/**
 * Destinations that must show the cross-sell bottom sheet after finishing some flow
 */
private val crossSellBottomSheetPermittingDestinations: List<KClass<out Destination>> = buildList {
  // Screens that a member will end up in after finishing any of the following flows
  // 1. Moving flow
  // 2. Edit co-insured
  // 3. Add/Upgrade addon
  // 4. Change tier
  addAll(insurancesCrossSellBottomSheetPermittingDestinations)
  addAll(helpCenterCrossSellBottomSheetPermittingDestinations)
  // One could finish those flows after a deep link, so the app's start destination must also be included
  addAll(homeCrossSellBottomSheetPermittingDestinations)
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
