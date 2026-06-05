package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.NavigationSuiteType
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@Composable
internal fun rememberHedvigAppState(
  backstackController: BackstackController,
  windowSizeClass: WindowSizeClass,
  settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): HedvigAppState {
  val appState = remember(
    backstackController,
    windowSizeClass,
    coroutineScope,
    settingsDataStore,
    getOnlyHasNonPayingContractsUseCase,
    featureManager,
    missedPaymentNotificationServiceProvider,
  ) {
    HedvigAppState(
      backstackController = backstackController,
      windowSizeClass = windowSizeClass,
      coroutineScope = coroutineScope,
      settingsDataStore = settingsDataStore,
      getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
      featureManager = featureManager,
      missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
    )
  }
  return appState
}

@Stable
internal class HedvigAppState(
  val backstackController: BackstackController,
  val windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope,
  private val settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>,
) {
  val navigationSuiteType: NavigationSuiteType
    get() = when (windowSizeClass.widthSizeClass) {
      WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar

      else -> when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Expanded -> NavigationSuiteType.NavigationRailXLarge
        else -> NavigationSuiteType.NavigationRail
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
    get() = backstackController.currentDestination is CrossSellEligibleDestination

  val topLevelTabs: StateFlow<Set<TopLevelTab>> = flow {
    val onlyHasNonPayingContracts = getOnlyHasNonPayingContractsUseCase.provide().invoke().getOrNull()
    emit(
      buildList {
        add(TopLevelTab.Home)
        add(TopLevelTab.Insurances)
        if (onlyHasNonPayingContracts != true) {
          add(TopLevelTab.Forever)
        }
        add(TopLevelTab.Payments)
        add(TopLevelTab.Profile)
      }.toSet(),
    )
  }.stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    setOf(
      TopLevelTab.Home,
      TopLevelTab.Insurances,
      TopLevelTab.Payments,
      TopLevelTab.Profile,
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
