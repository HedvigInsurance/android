package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import com.hedvig.android.app.navigation.HedvigTopLevelBackStacks
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
import com.hedvig.android.feature.payments.navigation.PaymentsDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.profileBottomNavPermittedDestinations
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationServiceProvider
import com.hedvig.android.theme.Theme
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@Composable
internal fun rememberHedvigAppState(
  backStacks: HedvigTopLevelBackStacks,
  windowSizeClass: WindowSizeClass,
  settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  missedPaymentNotificationServiceProvider: MissedPaymentNotificationServiceProvider,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): HedvigAppState {
  val appState = remember(
    backStacks,
    windowSizeClass,
    coroutineScope,
    settingsDataStore,
    getOnlyHasNonPayingContractsUseCase,
    featureManager,
    missedPaymentNotificationServiceProvider,
  ) {
    HedvigAppState(
      backStacks = backStacks,
      windowSizeClass = windowSizeClass,
      coroutineScope = coroutineScope,
      settingsDataStore = settingsDataStore,
      getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
      featureManager = featureManager,
      missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
    )
  }
  LaunchedEffect(appState) {
    snapshotFlow { appState.currentDestination }.collect { destination ->
      logcat { "Navigated to destination:$destination" }
      CurrentDestinationInMemoryStorage.currentDestination = destination
    }
  }
  return appState
}

@Stable
internal class HedvigAppState(
  val backStacks: HedvigTopLevelBackStacks,
  val windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope,
  private val settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  missedPaymentNotificationServiceProvider: MissedPaymentNotificationServiceProvider,
) {
  val currentDestination: HedvigNavKey?
    get() = backStacks.currentDestination

  val currentTopLevelGraph: TopLevelGraph
    get() = backStacks.currentTopLevel

  private val shouldShowNavBars: Boolean
    get() {
      if (!backStacks.isLoggedIn) return false
      val destination = currentDestination ?: return false
      if (destination.isTopLevelStartDestination()) return true
      return bottomNavPermittedDestinations.any { it.isInstance(destination) }
    }

  val navigationSuiteType: NavigationSuiteType
    get() {
      if (!shouldShowNavBars) return NavigationSuiteType.None
      return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar

        else -> when (windowSizeClass.heightSizeClass) {
          WindowHeightSizeClass.Expanded -> NavigationSuiteType.NavigationRailXLarge
          else -> NavigationSuiteType.NavigationRail
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
      SharingStarted.WhileSubscribed(5_000),
      false,
    )

  val isInScreenEligibleForCrossSells: Boolean
    get() {
      val destination = currentDestination ?: return false
      return crossSellBottomSheetPermittingDestinations.any { it.isInstance(destination) }
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

  val showPaymentsBadge: StateFlow<Boolean> = flow {
    val service = missedPaymentNotificationServiceProvider
      .provide()
    emitAll(service.showRedDotNotification())
  }.stateIn(
    coroutineScope,
    SharingStarted.WhileSubscribed(5_000),
    false,
  )

  /**
   * Navigate to a top level destination. Each tab keeps its own back stack; selecting the current
   * tab again pops it back to its start.
   */
  fun navigateToTopLevelGraph(topLevelGraph: TopLevelGraph) {
    backStacks.selectTopLevel(topLevelGraph)
  }

  fun navigateToLoggedIn() {
    backStacks.setLoggedIn()
  }

  fun navigateToLoggedOut() {
    backStacks.setLoggedOut()
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

private fun HedvigNavKey.isTopLevelStartDestination(): Boolean {
  return when (this) {
    is HomeDestination.Home,
    is InsurancesDestination.Insurances,
    is ForeverDestination.Forever,
    is PaymentsDestination.Payments,
    is ProfileDestination.Profile,
    -> true

    else -> false
  }
}

/**
 * Special routes, which despite not being top level should still show the navigation bars.
 */
private val bottomNavPermittedDestinations: List<KClass<out HedvigNavKey>> = buildList {
  addAll(profileBottomNavPermittedDestinations)
  addAll(com.hedvig.android.feature.claimhistory.nav.profileBottomNavPermittedDestinations)
  addAll(insurancesBottomNavPermittedDestinations)
}

/**
 * Destinations that must show the cross-sell bottom sheet after finishing some flow
 */
private val crossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = buildList {
  // Screens that a member will end up in after finishing any of the following flows
  // 1. Moving flow
  // 2. Edit co-insured
  // 3. Add/Upgrade addon
  // 4. Change tier
  addAll(insurancesCrossSellBottomSheetPermittingDestinations)
  addAll(helpCenterCrossSellBottomSheetPermittingDestinations)
  addAll(travelCertificateCrossSellBottomSheetPermittingDestinations)
  // One could finish those flows after a deep link, so the app's start destination must also be included
  addAll(homeCrossSellBottomSheetPermittingDestinations)
}
