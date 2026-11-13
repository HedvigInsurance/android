package com.hedvig.android.desktop.walkthrough

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.design.system.hedvig.datepicker.LocalAppLocale
import com.hedvig.android.desktop.DesktopAppHost
import com.hedvig.android.desktop.DesktopBackInput
import com.hedvig.android.desktop.DesktopExternalNavigator
import com.hedvig.android.desktop.DesktopGraph
import com.hedvig.android.desktop.desktopFallbackEntry
import com.hedvig.android.desktop.mouseBackButton
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import java.util.Locale

/**
 * The content of the desktop window as `Main.kt` composes it, with the window size class computed from the scene
 * size. [backInput] is registered with the navigation event dispatcher and the mouse back button, as the window does.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun WalkthroughApp(
  graph: DesktopGraph,
  backstackController: BackstackController,
  spec: SceneSpec,
  appHost: DesktopAppHost,
  externalNavigator: DesktopExternalNavigator,
  backInput: DesktopBackInput,
) {
  val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
  DisposableEffect(navigationEventDispatcher) {
    navigationEventDispatcher?.addInput(backInput)
    onDispose { navigationEventDispatcher?.removeInput(backInput) }
  }
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(graph, lifecycle) {
    graph.sessionReconciler.reconcile()
    graph.sessionReconciler.observeForcedLogout(lifecycle)
  }
  val appLanguage by graph.languageService.language.collectAsState()
  CompositionLocalProvider(
    LocalMetroViewModelFactory provides graph.metroViewModelFactory,
    LocalAppLocale provides remember(appLanguage) { Locale.forLanguageTag(appLanguage.toBcp47Format()) },
  ) {
    key(appLanguage) {
      Box(Modifier.fillMaxSize().mouseBackButton(backInput)) {
        HedvigApp(
          backstackController = backstackController,
          deepLinkReadySignal = graph.sessionReconciler.isReady,
          windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(spec.widthDp.dp, spec.heightDp.dp)),
          settingsDataStore = graph.settingsDataStore,
          featureManager = graph.featureManager,
          authTokenService = graph.authTokenService,
          demoManager = graph.demoManager,
          memberIdService = graph.memberIdService,
          deepLinkMatcher = graph.deepLinkMatcher,
          imageLoader = graph.imageLoader,
          languageService = graph.languageService,
          hedvigBuildConstants = graph.hedvigBuildConstants,
          waitUntilAppReviewDialogShouldBeOpenedUseCase = graph.waitUntilAppReviewDialogShouldBeOpenedUseCase,
          appHost = appHost,
          externalNavigator = externalNavigator,
          logoutUseCase = graph.logoutUseCase,
          getMemberAuthorizationCodeUseCase = graph.getMemberAuthorizationCodeUseCase,
          paymentsNotificationBadgeService = graph.paymentsNotificationBadgeService,
          currentDestinationHolder = graph.currentDestinationHolder,
          eventTrackingClient = graph.eventTrackingClient,
          screenParameterExtractor = graph.screenParameterExtractor,
          onboardingGate = graph.onboardingGate,
          resetOnboardingSeenUseCase = graph.resetOnboardingSeenUseCase,
          fallbackEntry = remember(backstackController) { desktopFallbackEntry(backstackController) },
        )
      }
    }
  }
}
