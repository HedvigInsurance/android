package com.hedvig.android.app.ui

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.datasource.cache.SimpleCache
import arrow.fx.coroutines.raceN
import coil3.ImageLoader
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.urihandler.DeepLinkFirstUriHandler
import com.hedvig.android.app.urihandler.SafeAndroidUriHandler
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheet
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.compose.DeepLinkMatcherProvider
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.ui.force.upgrade.ForceUpgradeBlockingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HedvigApp(
  backstackController: BackstackController,
  deepLinkChannel: Channel<String>,
  windowSizeClass: WindowSizeClass,
  settingsDataStore: SettingsDataStore,
  getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>,
  featureManager: FeatureManager,
  splashIsRemovedSignal: Channel<Unit>,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  deepLinkMatcherProviders: Set<DeepLinkMatcherProvider>,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase,
  enableEdgeToEdge: (SystemBarStyle) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  finishApp: () -> Unit,
  tryShowAppStoreReviewDialog: () -> Unit,
  externalNavigator: ExternalNavigator,
  logoutUseCase: LogoutUseCase,
  getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>,
  dismissSplashScreen: () -> Unit,
) {
  val hedvigAppState = rememberHedvigAppState(
    backstackController = backstackController,
    windowSizeClass = windowSizeClass,
    settingsDataStore = settingsDataStore,
    getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
    featureManager = featureManager,
    missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
  )
  DetermineStartDestinationEffect(
    backstackController = backstackController,
    authTokenService = authTokenService,
    demoManager = demoManager,
    onLoggedIn = hedvigAppState::navigateToLoggedIn,
    onLoggedOut = hedvigAppState::navigateToLoggedOut,
    dismissSplashScreen = dismissSplashScreen,
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
      val deepLinkMatcher = remember(deepLinkMatcherProviders) {
        HedvigDeepLinkMatcher(deepLinkMatcherProviders.flatMap { it.matchers() })
      }
      val deepLinkFirstUriHandler = DeepLinkFirstUriHandler(
        matcher = deepLinkMatcher,
        backstackController = backstackController,
        delegate = SafeAndroidUriHandler(LocalContext.current),
      )
      LaunchedEffect(deepLinkFirstUriHandler, backstackController, deepLinkChannel) {
        deepLinkChannel.receiveAsFlow().collect { uri ->
          // Buffer external/notification deep links until the member is logged in, so they don't
          // land on (and get cleared with) the login back stack.
          snapshotFlow { backstackController.isLoggedIn }.first { it }
          deepLinkFirstUriHandler.openUri(uri)
        }
      }
      val scope = rememberCoroutineScope()
      val openCrossSellUrl: (String) -> Unit = { url ->
        openCrossSellUrl(scope, getMemberAuthorizationCodeUseCase, deepLinkFirstUriHandler, url)
      }
      CrossSellSheet(
        isInScreenEligibleForCrossSells = hedvigAppState.isInScreenEligibleForCrossSells,
        onCrossSellClick = openCrossSellUrl,
        imageLoader,
//        onNavigateToAddonPurchaseFlow = { insuranceIds ->
//          navHostController.navigate(
//            AddonPurchaseGraphDestination(
//              insuranceIds,
//              TravelAddonBannerSource.AFTER_FINISHING_SUCCESSFUL_FLOW,
//            ),
//          )
//        },
      )
      SharedTransitionLayout(Modifier.fillMaxSize()) {
        CompositionLocalProvider(
          LocalUriHandler provides deepLinkFirstUriHandler,
          LocalSharedTransitionScope provides this,
        ) {
          HedvigAppUi(
            hedvigAppState = hedvigAppState,
            externalNavigator = externalNavigator,
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
            openUrl = deepLinkFirstUriHandler::openUri,
            openCrossSellUrl = openCrossSellUrl,
            finishApp = finishApp,
            imageLoader = imageLoader,
            languageService = languageService,
            hedvigBuildConstants = hedvigBuildConstants,
            simpleVideoCache = simpleVideoCache,
            demoManager = demoManager,
            logoutUseCase = logoutUseCase,
          )
        }
      }
    }
  }
}

private fun openCrossSellUrl(
  scope: CoroutineScope,
  getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  deepLinkFirstUriHandler: DeepLinkFirstUriHandler,
  url: String,
) {
  scope.launch {
    val code = getMemberAuthorizationCodeUseCase.invoke()
    val finalUrl = if (code != null) {
      url.toUri().buildUpon().appendQueryParameter("authorization_code", code).build().toString()
    } else {
      url
    }
    deepLinkFirstUriHandler.openUri(finalUrl)
  }
}

/**
 * Holds the splash screen until the auth state resolves, then makes the back stack root match it
 * before [dismissSplashScreen] lets the splash go — so the first frame shown is the correct scene
 * (Home when logged in, Login when logged out) instead of the seeded login root.
 *
 * On process-death restore the back stack already reflects the previous session, so a matching root
 * is left untouched and any deeper stack is preserved.
 */
@Composable
private fun DetermineStartDestinationEffect(
  backstackController: BackstackController,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  onLoggedIn: () -> Unit,
  onLoggedOut: () -> Unit,
  dismissSplashScreen: () -> Unit,
) {
  LaunchedEffect(Unit) {
    val showLoggedInScene = raceN(
      { authTokenService.authStatus.filterNotNull().first() is AuthStatus.LoggedIn },
      { demoManager.isDemoMode().first { it } },
    ).fold({ it }, { it })
    when {
      showLoggedInScene && !backstackController.isLoggedIn -> onLoggedIn()
      !showLoggedInScene && backstackController.isLoggedIn -> onLoggedOut()
    }
    dismissSplashScreen()
  }
}

/**
 * Temporary measure as both design systems need to live side-by-side.
 * When everything can come from com.hedvig.android.design.system.hedvig, then this can potentially be removed.
 */
@Composable
private fun HedvigTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
  com.hedvig.android.design.system.hedvig.HedvigTheme(darkTheme = darkTheme) {
    content()
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
        append("Owner: MainActivity | Received authStatus: ")
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
        snapshotFlow { hedvigAppState.backstackController.isLoggedIn },
      ) { authStatus: AuthStatus, isDemoMode: Boolean, isLoggedIn: Boolean ->
        logcat {
          "LogoutOnInvalidCredentialsEffect: " +
            "authStatus:$authStatus | " +
            "isDemoMode:$isDemoMode | " +
            "isLoggedIn:$isLoggedIn"
        }
        if (!isLoggedIn) {
          return@combine
        }
        if (!isDemoMode && authStatus !is AuthStatus.LoggedIn) {
          hedvigAppState.navigateToLoggedOut()
        }
      }.collect()
    }
  }
}
