package com.hedvig.android.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.design.system.hedvig.datepicker.LocalAppLocale
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import java.io.File
import java.util.Locale

fun main() {
  val backend = if (requiredSystemProperty("hedvig.desktop.production").toBooleanStrict()) {
    DesktopBackend.Production
  } else {
    DesktopBackend.Staging
  }
  installDesktopLogging(stdoutLogcatLogger(backend))
  Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
    logcat(LogPriority.ERROR, throwable) { "Uncaught exception on thread ${thread.name}" }
  }
  val appBuildConfig = DesktopAppBuildConfig(
    backend = backend,
    versionName = requiredSystemProperty("hedvig.desktop.versionName"),
    versionCode = requiredSystemProperty("hedvig.desktop.versionCode").toInt(),
  )
  val dataDirectory = File(System.getProperty("user.home"), ".hedvig-desktop/${backend.directoryName}")
  dataDirectory.mkdirs()
  logcat { "Data directory: ${dataDirectory.absolutePath}" }
  val backstackController = newBackstackController()
  backstackController.seedIfEmpty(listOf(LoginKey))
  val graph = createGraphFactory<DesktopGraph.Factory>().create(backstackController, appBuildConfig, dataDirectory)
  with(graph.hedvigBuildConstants) {
    logcat {
      "Backend identity: X-Platform=$platformName | User-Agent=$userAgent | " +
        "Hedvig-App-Version=${platformName.lowercase()};$appVersionName | GraphQL=$urlGraphqlOctopus"
    }
  }
  for (initializable in graph.initializables) {
    initializable.initialize()
  }
  val unmatchedLinkRouter = DesktopUnmatchedLinkRouter(
    runningFlavor = appBuildConfig.appFlavor,
    matcher = graph.deepLinkMatcher,
    backstackController = backstackController,
  )
  val appHost = DesktopAppHost(routeUnmatchedLink = unmatchedLinkRouter::route)
  val externalNavigator = DesktopExternalNavigator()
  val backInput = DesktopBackInput()
  application {
    backstackController.finishApp = ::exitApplication
    Window(
      onCloseRequest = ::exitApplication,
      title = "Hedvig",
      state = rememberWindowState(size = initialWindowSize(usableScreenHeight())),
      onKeyEvent = backInput::handle,
    ) {
      val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
      DisposableEffect(navigationEventDispatcher) {
        navigationEventDispatcher?.addInput(backInput)
        onDispose { navigationEventDispatcher?.removeInput(backInput) }
      }
      Box(Modifier.fillMaxSize().mouseBackButton(backInput)) {
        DesktopApp(graph, backstackController, appHost, externalNavigator)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun DesktopApp(
  graph: DesktopGraph,
  backstackController: BackstackController,
  appHost: DesktopAppHost,
  externalNavigator: ExternalNavigator,
) {
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
    // Compose resources read the locale while composing without observing it, so a language change recomposes the
    // app from here. The back stack lives in the controller and survives it.
    key(appLanguage) {
      HedvigApp(
        backstackController = backstackController,
        deepLinkReadySignal = graph.sessionReconciler.isReady,
        windowSizeClass = calculateWindowSizeClass(),
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

private fun requiredSystemProperty(name: String): String = System.getProperty(name)
  ?: error("System property $name is missing. Start the desktop app with ./gradlew :app-desktop:run")
