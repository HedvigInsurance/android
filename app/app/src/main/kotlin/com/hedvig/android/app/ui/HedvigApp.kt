package com.hedvig.android.app.ui

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import com.hedvig.android.app.AndroidAppHost
import com.hedvig.android.app.GlobalHedvigSnackBar
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.navigation.CurrentDestinationHolder
import com.hedvig.android.app.navigation.hedvigEntryProvider
import com.hedvig.android.app.navigation.shouldFadeThrough
import com.hedvig.android.app.urihandler.AuthorizationCodeUriHandler
import com.hedvig.android.app.urihandler.DeepLinkFirstUriHandler
import com.hedvig.android.app.urihandler.SafeAndroidUriHandler
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.DemoModeLabel
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.design.system.hedvig.rememberGlobalSnackBarState
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheet
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.navigation.compose.entryDecorators
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.ui.force.upgrade.ForceUpgradeBlockingScreen
import hedvig.resources.EXIT_DEMO_MODE_BUTTON
import hedvig.resources.Res
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringResource

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
  memberIdService: MemberIdService,
  deepLinkMatcher: HedvigDeepLinkMatcher,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase,
  androidAppHost: AndroidAppHost,
  externalNavigator: ExternalNavigator,
  logoutUseCase: LogoutUseCase,
  getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase,
  missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>,
  currentDestinationHolder: CurrentDestinationHolder,
) {
  ReportCurrentDestinationEffect(backstackController, currentDestinationHolder)
  val hedvigAppState = rememberHedvigAppState(
    backstackController = backstackController,
    windowSizeClass = windowSizeClass,
    settingsDataStore = settingsDataStore,
    getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
    featureManager = featureManager,
    missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
  )
  val darkTheme = hedvigAppState.darkTheme
  HedvigTheme(darkTheme = darkTheme) {
    EnableEdgeToEdgeSideEffect(darkTheme, splashIsRemovedSignal, androidAppHost::applyEdgeToEdgeStyle)
    val mustForceUpdate by hedvigAppState.mustForceUpdate.collectAsStateWithLifecycle()
    if (mustForceUpdate) {
      ForceUpgradeBlockingScreen(
        goToPlayStore = externalNavigator::tryOpenPlayStore,
      )
    } else {
      TryShowAppStoreReviewDialogEffect(
        authTokenService,
        waitUntilAppReviewDialogShouldBeOpenedUseCase,
        androidAppHost::tryShowAppStoreReviewDialog,
      )
      val scope = rememberCoroutineScope()
      val context = LocalContext.current
      val deepLinkFirstUriHandler = remember(deepLinkMatcher, backstackController, context) {
        DeepLinkFirstUriHandler(
          matcher = deepLinkMatcher,
          backstackController = backstackController,
          delegate = SafeAndroidUriHandler(context),
        )
      }
      val authorizationCodeUriHandler = remember(getMemberAuthorizationCodeUseCase, deepLinkFirstUriHandler, scope) {
        AuthorizationCodeUriHandler(
          getMemberAuthorizationCodeUseCase = getMemberAuthorizationCodeUseCase,
          delegate = deepLinkFirstUriHandler,
          scope = scope,
        )
      }
      LaunchedEffect(deepLinkFirstUriHandler, deepLinkChannel) {
        deepLinkChannel.receiveAsFlow().collect { uri ->
          deepLinkFirstUriHandler.openUri(uri)
        }
      }
      CrossSellSheet(
        isInScreenEligibleForCrossSells = hedvigAppState.isInScreenEligibleForCrossSells,
        onCrossSellClick = authorizationCodeUriHandler::openUri,
        imageLoader,
      )
      SharedTransitionLayout(Modifier.fillMaxSize()) {
        CompositionLocalProvider(
          LocalUriHandler provides authorizationCodeUriHandler,
          LocalSharedTransitionScope provides this,
        ) {
          val globalSnackBarState = rememberGlobalSnackBarState()
          val sceneDecoratorStrategies = rememberHedvigChromeStrategy(
            hedvigAppState = hedvigAppState,
            sharedTransitionScope = this@SharedTransitionLayout,
          )
          val density = LocalDensity.current
          val popSpec = hedvigPopTransitionSpec(backstackController, density)
          Box(Modifier.fillMaxSize()) {
            Surface(
              color = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.backgroundPrimary,
              contentColor = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.textPrimary,
            ) {
              Box(propagateMinConstraints = true, modifier = Modifier.fillMaxSize()) {
                GlobalHedvigSnackBar(globalSnackBarState = globalSnackBarState)
                NavDisplay(
                  backStack = backstackController.entries,
                  onBack = backstackController::popBackstack,
                  entryDecorators = entryDecorators { backstackController.allLiveContentKeys },
                  sharedTransitionScope = this@SharedTransitionLayout,
                  sceneDecoratorStrategies = sceneDecoratorStrategies,
                  transitionSpec = hedvigTransitionSpec(backstackController, density),
                  popTransitionSpec = popSpec,
                  predictivePopTransitionSpec = { popSpec() },
                  entryProvider = entryProvider {
                    hedvigEntryProvider(
                      backstack = backstackController,
                      scope = scope,
                      windowSizeClass = windowSizeClass,
                      memberIdService = memberIdService,
                      globalSnackBarState = globalSnackBarState,
                      externalNavigator = externalNavigator,
                      androidAppHost = androidAppHost,
                      openUrl = authorizationCodeUriHandler::openUri,
                      openCrossSellUrl = authorizationCodeUriHandler::openUri,
                      imageLoader = imageLoader,
                      languageService = languageService,
                      hedvigBuildConstants = hedvigBuildConstants,
                    )
                  },
                )
              }
            }
            DemoModeOverlay(demoManager, logoutUseCase)
          }
        }
      }
    }
  }
}

private fun hedvigTransitionSpec(
  backstack: BackstackController,
  density: Density,
): AnimatedContentTransitionScope<Scene<HedvigNavKey>>.() -> ContentTransform = {
  val fromTab = backstack.owningTopLevelTabForContentKey(initialState.entries.lastOrNull()?.contentKey)
  val toTab = backstack.owningTopLevelTabForContentKey(targetState.entries.lastOrNull()?.contentKey)
  if (shouldFadeThrough(fromTab, toTab)) {
    MotionDefaults.fadeThroughEnter togetherWith MotionDefaults.fadeThroughExit
  } else {
    MotionDefaults.sharedXAxisEnter(density) togetherWith MotionDefaults.sharedXAxisExit(density)
  }
}

private fun hedvigPopTransitionSpec(
  backstack: BackstackController,
  density: Density,
): AnimatedContentTransitionScope<Scene<HedvigNavKey>>.() -> ContentTransform = {
  val fromTab = backstack.owningTopLevelTabForContentKey(initialState.entries.lastOrNull()?.contentKey)
  val toTab = backstack.owningTopLevelTabForContentKey(targetState.entries.lastOrNull()?.contentKey)
  if (shouldFadeThrough(fromTab, toTab)) {
    MotionDefaults.fadeThroughEnter togetherWith MotionDefaults.fadeThroughExit
  } else {
    MotionDefaults.sharedXAxisPopEnter(density) togetherWith MotionDefaults.sharedXAxisPopExit(density)
  }
}

/**
 * Demo-mode affordance overlaid on top of the app content. Reads `isDemoMode` itself so the read
 * stays scoped here and doesn't recompose the hosted [NavDisplay].
 */
@Composable
private fun BoxScope.DemoModeOverlay(demoManager: DemoManager, logoutUseCase: LogoutUseCase) {
  val isDemoMode by demoManager.isDemoMode().collectAsState(false)
  if (isDemoMode) {
    DemoModeLabel(
      stringResource(Res.string.EXIT_DEMO_MODE_BUTTON),
      onButtonClick = { logoutUseCase.invoke() },
      modifier = Modifier
        .padding(start = 16.dp, end = 32.dp, bottom = 86.dp)
        .align(Alignment.BottomEnd)
        .windowInsetsPadding(WindowInsets.systemBars),
    )
  }
}

/**
 * Mirrors the destination on top of the rendered stack into [CurrentDestinationHolder] so non-Composable
 * consumers (e.g. the FCM-thread [com.hedvig.android.app.notification.senders.ChatNotificationSender]) can
 * read it. Kept non-persistent on purpose: a process death wipes it, which is the desired behavior.
 */
@Composable
private fun ReportCurrentDestinationEffect(
  backstackController: BackstackController,
  currentDestinationHolder: CurrentDestinationHolder,
) {
  LaunchedEffect(backstackController, currentDestinationHolder) {
    snapshotFlow { backstackController.currentDestination }.collect { destination ->
      logcat { "Navigated to destination:$destination" }
      currentDestinationHolder.update(destination)
    }
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
  val reviewDialogDelay = 2.seconds
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
      waitUntilAppReviewDialogShouldBeOpenedUseCase.invoke()
      delay(reviewDialogDelay)
      tryShowAppStoreReviewDialog()
    }
  }
}
