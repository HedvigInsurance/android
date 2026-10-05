package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.notification.badge.data.payment.PaymentsNotificationBadge
import com.hedvig.android.notification.badge.data.payment.PaymentsNotificationBadgeService
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@Composable
internal fun rememberHedvigAppState(
  backstackController: BackstackController,
  windowSizeClass: WindowSizeClass,
  settingsDataStore: SettingsDataStore,
  featureManager: FeatureManager,
  paymentsNotificationBadgeService: PaymentsNotificationBadgeService,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): HedvigAppState {
  val appState = remember(
    backstackController,
    windowSizeClass,
    coroutineScope,
    settingsDataStore,
    featureManager,
    paymentsNotificationBadgeService,
  ) {
    HedvigAppState(
      backstackController = backstackController,
      windowSizeClass = windowSizeClass,
      coroutineScope = coroutineScope,
      settingsDataStore = settingsDataStore,
      featureManager = featureManager,
      paymentsNotificationBadgeService = paymentsNotificationBadgeService,
    )
  }
  // Keyed on the instance, so a state replaced by a window size change stops collecting along with it.
  LaunchedEffect(appState) {
    appState.markChargeNoticesAsSeenWhileOnPayments()
  }
  return appState
}

@Stable
internal class HedvigAppState(
  val backstackController: BackstackController,
  val windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope,
  private val settingsDataStore: SettingsDataStore,
  featureManager: FeatureManager,
  private val paymentsNotificationBadgeService: PaymentsNotificationBadgeService,
) {
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

  val topLevelTabs: StateFlow<Set<TopLevelTab>> = MutableStateFlow(
    setOf(
      TopLevelTab.Home,
      TopLevelTab.Insurances,
      TopLevelTab.Forever,
      TopLevelTab.Payments,
      TopLevelTab.Profile,
    ),
  )

  val paymentsBadge: StateFlow<PaymentsNotificationBadge?> = paymentsNotificationBadgeService
    .badge()
    .stateIn(
      coroutineScope,
      SharingStarted.WhileSubscribed(5_000),
      null,
    )

  suspend fun markChargeNoticesAsSeenWhileOnPayments() {
    combine(
      snapshotFlow { backstackController.currentTopLevel },
      paymentsBadge,
    ) { currentTopLevel, badge ->
      currentTopLevel == TopLevelTab.Payments && badge == PaymentsNotificationBadge.ChargeNotice
    }
      .distinctUntilChanged()
      .filter { isOnPaymentsWithChargeNotice -> isOnPaymentsWithChargeNotice }
      .collect { paymentsNotificationBadgeService.markChargeNoticesAsSeen() }
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
