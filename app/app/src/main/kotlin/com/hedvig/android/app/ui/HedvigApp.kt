package com.hedvig.android.app.ui

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.hedvig.android.app.urihandler.DeepLinkFirstUriHandler
import com.hedvig.android.app.urihandler.SafeAndroidUriHandler
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.force.upgrade.ForceUpgradeBlockingScreen
import com.hedvig.android.feature.login.navigation.LoginDestination
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.compose.typedHasRoute
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
internal fun HedvigApp(
  navHostController: NavHostController,
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  splashIsRemovedSignal: Channel<Unit>,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase,
  enableEdgeToEdge: (SystemBarStyle) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  finishApp: () -> Unit,
  tryShowAppStoreReviewDialog: () -> Unit,
  externalNavigator: ExternalNavigator,
) {
  val hedvigAppState = rememberHedvigAppState(
    windowSizeClass = windowSizeClass,
    tabNotificationBadgeService = tabNotificationBadgeService,
    settingsDataStore = settingsDataStore,
    getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
    featureManager = featureManager,
    navHostController = navHostController,
  )
  val darkTheme = hedvigAppState.darkTheme
  HedvigTheme(darkTheme = darkTheme) {
    EnableEdgeToEdgeSideEffect(darkTheme, splashIsRemovedSignal, enableEdgeToEdge)
    val mustForceUpdate by hedvigAppState.mustForceUpdate.collectAsStateWithLifecycle()
    if (mustForceUpdate) {
      ForceUpgradeBlockingScreen(
        goToPlayStore = externalNavigator::tryOpenPlayStore,
      )
    } else {
      TryShowAppStoreReviewDialogEffect(
        authTokenService,
        waitUntilAppReviewDialogShouldBeOpenedUseCase,
        tryShowAppStoreReviewDialog,
      )
      LogoutOnInvalidCredentialsEffect(hedvigAppState, authTokenService, demoManager)
      val deepLinkFirstUriHandler = DeepLinkFirstUriHandler(
        navController = hedvigAppState.navController,
        delegate = SafeAndroidUriHandler(LocalContext.current),
      )
      CompositionLocalProvider(LocalUriHandler provides deepLinkFirstUriHandler) {
        HedvigAppUi(
          hedvigAppState = hedvigAppState,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          externalNavigator = externalNavigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openUrl = deepLinkFirstUriHandler::openUri,
          finishApp = finishApp,
          market = marketManager.market.collectAsStateWithLifecycle().value,
          imageLoader = imageLoader,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
        )
      }
    }
  }
}

/**
 * Temporary measure as both design systems need to live side-by-side.
 * When everything can come from com.hedvig.android.design.system.hedvig, then this can potentially be removed.
 */
@Composable
private fun HedvigTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
  com.hedvig.android.core.designsystem.theme.HedvigTheme(darkTheme = darkTheme) {
    com.hedvig.android.design.system.hedvig.HedvigTheme(darkTheme = darkTheme) {
      content()
    }
  }
}

@Composable
private fun EnableEdgeToEdgeSideEffect(
  darkTheme: Boolean,
  splashIsRemovedSignal: Channel<Unit>,
  enableEdgeToEdge: (SystemBarStyle) -> Unit,
) {
  val splashIsRemovedIndex by produceState(0) {
    splashIsRemovedSignal.receiveAsFlow().collectLatest { value = value + 1 }
  }
  DisposableEffect(darkTheme, splashIsRemovedIndex) {
    enableEdgeToEdge(
      when (darkTheme) {
        true -> SystemBarStyle.dark(Color.TRANSPARENT)
        false -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
      },
    )
    onDispose {}
  }
}

@Composable
private fun TryShowAppStoreReviewDialogEffect(
  authTokenService: AuthTokenService,
  waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase,
  tryShowAppStoreReviewDialog: () -> Unit,
) {
  val REVIEW_DIALOG_DELAY_MILLIS = 2000L
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
      waitUntilAppReviewDialogShouldBeOpenedUseCase.invoke()
      delay(REVIEW_DIALOG_DELAY_MILLIS)
      tryShowAppStoreReviewDialog()
    }
  }
}

/**
 * Automatically logs out when we are no longer in demo mode and we are also not considered to have active tokens
 */
@Composable
private fun LogoutOnInvalidCredentialsEffect(
  hedvigAppState: HedvigAppState,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
) {
  val authStatusLog: (AuthStatus?) -> Unit = { authStatus ->
    logcat {
      buildString {
        append("Owner: LoggedInActivity | Received authStatus: ")
        append(
          when (authStatus) {
            is AuthStatus.LoggedIn -> "LoggedIn"
            AuthStatus.LoggedOut -> "LoggedOut"
            null -> "null"
          },
        )
      }
    }
  }
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(lifecycle, hedvigAppState, authTokenService, demoManager) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
      combine(
        authTokenService.authStatus.onEach(authStatusLog).filterNotNull().distinctUntilChanged(),
        demoManager.isDemoMode().distinctUntilChanged(),
      ) { authStatus: AuthStatus, isDemoMode: Boolean ->
        authStatus to isDemoMode
      }.collect { (authStatus, isDemoMode) ->
        val navBackStackEntry: NavBackStackEntry = hedvigAppState.navController.currentBackStackEntryFlow.first()
        val isLoggedOut = navBackStackEntry.destination.hierarchy.any { navDestination ->
          navDestination.typedHasRoute<LoginDestination>()
        }
        if (isLoggedOut) {
          return@collect
        }
        if (!isDemoMode && authStatus !is AuthStatus.LoggedIn) {
          hedvigAppState.navigateToLoggedOut()
        }
      }
    }
  }
}
