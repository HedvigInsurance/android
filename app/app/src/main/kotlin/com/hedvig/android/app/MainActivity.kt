package com.hedvig.android.app

import android.app.UiModeManager
import android.app.UiModeManager.MODE_NIGHT_CUSTOM
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.ImageLoader
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.Navigation3TrackingEffect
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.externalnavigator.ExternalNavigatorImpl
import com.hedvig.android.app.navigation.AnalyticsNamePredicate
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.navigation.CurrentDestinationHolder
import com.hedvig.android.app.navigation.NavRetainedViewModel
import com.hedvig.android.app.navigation.ScreenParameterExtractor
import com.hedvig.android.app.navigation.androidHostEntries
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.app.urihandler.ExternalDeepLinkHandler
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.compose.ui.LocalClock
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.rive.RiveInitializer
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.design.system.hedvig.datepicker.LocalAppLocale
import com.hedvig.android.feature.onboarding.data.ResetOnboardingSeenUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.withAppLanguage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.notification.badge.data.payment.PaymentsNotificationBadgeService
import com.hedvig.android.theme.Theme
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import java.util.Locale
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule

class MainActivity : AppCompatActivity() {
  @Inject
  private lateinit var authTokenService: AuthTokenService

  @Inject
  private lateinit var demoManager: DemoManager

  @Inject
  private lateinit var featureManager: FeatureManager

  @Inject
  private lateinit var hedvigBuildConstants: HedvigBuildConstants

  @Inject
  private lateinit var deepLinkMatcher: HedvigDeepLinkMatcher

  @Inject
  private lateinit var imageLoader: ImageLoader

  @Inject
  private lateinit var languageService: LanguageService

  @Inject
  private lateinit var settingsDataStore: SettingsDataStore

  @Inject
  private lateinit var waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase

  @Inject
  private lateinit var logoutUseCase: LogoutUseCase

  @Inject
  private lateinit var getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase

  @Inject
  private lateinit var memberIdService: MemberIdService

  @Inject
  private lateinit var paymentsNotificationBadgeService: PaymentsNotificationBadgeService

  @Inject
  private lateinit var currentDestinationHolder: CurrentDestinationHolder

  @Inject
  private lateinit var eventTrackingClient: EventTrackingClient

  @Inject
  private lateinit var screenParameterExtractor: ScreenParameterExtractor

  @Inject
  private lateinit var serializersModules: Set<SerializersModule>

  @Inject
  private lateinit var resetOnboardingSeenUseCase: ResetOnboardingSeenUseCase

  @Inject
  private lateinit var clock: Clock

  /**
   * Per-Activity host for the navigation state. A retained `ViewModel`, so it (and the
   * [BackstackController] it owns) survives a config change but dies with this Activity — giving each
   * `MainActivity` instance its own back stack instead of sharing one app-singleton. Resolved at the
   * top of [onCreate]; the controller, session reconciler and merged ViewModel factory are read off it.
   */
  private val navRetainedViewModel: NavRetainedViewModel by lazy {
    ViewModelProvider(
      this,
      viewModelFactory {
        initializer { NavRetainedViewModel((application as HedvigApplication).appGraph) }
      },
    )[NavRetainedViewModel::class.java]
  }

  private val backstackController get() = navRetainedViewModel.backstackController
  private val sessionReconciler get() = navRetainedViewModel.sessionReconciler

  /**
   * External/notification VIEW intents are forwarded here as raw URI strings, with the flags of the
   * intent that delivered them. The per-Activity collector in [onCreate] routes each through
   * [externalDeepLinkHandler] once the start scene is resolved.
   */
  private val deepLinkChannel = Channel<ExternalDeepLink>(Channel.UNLIMITED)

  /**
   * Per-Activity router for external/notification deep links. Lives outside composition because it is
   * auth/navigation reconciliation — a sibling of [sessionReconciler] — not UI, and only mutates this Activity's
   * back stack. Lazy because it reads @Inject fields, which are only populated after `appGraph.inject(this)`.
   */
  private val externalDeepLinkHandler: ExternalDeepLinkHandler by lazy {
    ExternalDeepLinkHandler(
      matcher = deepLinkMatcher,
      backstackController = backstackController,
      readySignal = sessionReconciler.isReady,
      deepLinkHosts = hedvigBuildConstants.deepLinkHosts,
    )
  }

  /**
   * A channel to report whenever the splash screen has stopped showing. This is used to let `enableEdgeToEdge` be run
   * again, this time properly taking into account the current background color, and not whatever might have been
   * showing in the splash screen itself. Without this, `enableEdgeToEdge` might try to compensate for the background
   * color if it turns out to be different from what the app background color is, since the night/day theme may have
   * been overwritten from the in-app settings.
   */
  private val splashIsRemovedSignal = Channel<Unit>(Channel.UNLIMITED)

  /**
   * Per-Activity host for finish/relaunch mechanics, shared between the task hooks and HedvigApp.
   * Lazy on purpose: a plain `val` initializer runs during construction, before `appGraph.inject(this)`
   * in onCreate, so it must not touch any @Inject field. Deferring to first access (in/after onCreate)
   * keeps this safe even if someone later gives AndroidAppHostImpl an injected dependency.
   */
  private val androidAppHost: AndroidAppHostImpl by lazy { AndroidAppHostImpl(this, splashIsRemovedSignal) }

  override fun onCreate(savedInstanceState: Bundle?) {
    (application as HedvigApplication).appGraph.inject(this)
    installSplashScreen().apply {
      setKeepOnScreenCondition { !sessionReconciler.isReady.value }
      setOnExitAnimationListener {
        logcat(LogPriority.INFO) { "Splash screen will be removed" }
        it.remove()
        splashIsRemovedSignal.trySend(Unit)
      }
    }
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    ComposeFoundationFlags.isNewContextMenuEnabled = false
    super.onCreate(savedInstanceState)
    logcat(LogPriority.INFO) {
      "MainActivity@${System.identityHashCode(this)} using " +
        "BackstackController@${System.identityHashCode(backstackController)}"
    }
    val uiModeManager = getSystemService<UiModeManager>()
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        settingsDataStore.observeTheme().collectLatest { theme ->
          applyTheme(theme, uiModeManager)
        }
      }
    }
    val launchedFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
    logcat(priority = LogPriority.VERBOSE, tag = DEEP_LINK_STACK_DEBUG_TAG) {
      "MainActivity.onCreate launch context: " +
        "isColdStart(savedInstanceState==null)=${savedInstanceState == null} | " +
        "isTaskRoot=$isTaskRoot | " +
        "intent.action=${intent.action} | " +
        "intent.categories=${intent.categories} | " +
        "intent.data=${intent.data} | " +
        "launchedFromHistory=$launchedFromHistory | " +
        "intent.flags=0x${Integer.toHexString(intent.flags)}"
    }
    if (savedInstanceState == null) {
      handleDeepLinkIntent(intent)
    }
    // onNewIntent fires (instead of a fresh onCreate) when the existing instance is reused: a caller
    // sets FLAG_ACTIVITY_SINGLE_TOP while this Activity is top-of-task, or the launch config gains a
    // singleTop/singleTask launchMode. None of our own callers (notification PendingIntents, App Link
    // ACTION_VIEW) set that flag today, so those currently arrive via onCreate above — but an external
    // caller can still trigger this path. Routed through the same handler either way so a deep link is
    // never silently dropped.
    addOnNewIntentListener { newIntent -> handleDeepLinkIntent(newIntent) }

    backstackController.escapeToOwnTask = { parentStack ->
      NavigationStateBridge.escapeToOwnTask(this@MainActivity, parentStack, serializersModules)
    }
    backstackController.finishApp = androidAppHost::finishApp
    val externalNavigator = ExternalNavigatorImpl(this, hedvigBuildConstants.appPackageId, languageService)
    RiveInitializer.init(this)
    NavigationStateBridge.restoreAndPersist(
      backstackController = backstackController,
      savedStateRegistry = savedStateRegistry,
      intent = intent,
      isColdStart = savedInstanceState == null,
      serializersModules = serializersModules,
      clock = clock,
    )
    lifecycleScope.launch {
      sessionReconciler.reconcile()
      sessionReconciler.observeForcedLogout(lifecycle)
    }
    lifecycleScope.launch {
      deepLinkChannel.receiveAsFlow().collect { deepLink ->
        externalDeepLinkHandler.handle(deepLink.uri) { isHostedInForeignTask(isTaskRoot, deepLink.launchFlags) }
      }
    }
    setContent {
      val appLanguage by languageService.language.collectAsState()
      CompositionLocalProvider(
        LocalMetroViewModelFactory provides navRetainedViewModel.viewModelFactory,
        LocalAppLocale provides remember(appLanguage) { Locale.forLanguageTag(appLanguage.toBcp47Format()) },
        LocalResources provides rememberAppLanguageResources(appLanguage),
        LocalClock provides clock,
      ) {
        // Compute the window size class from Configuration. Do not switch this to
        // calculateWindowSizeClass(activity) or LocalWindowInfo.containerSize: both route through
        // androidx.window's WindowMetricsCalculator, which invokes WindowMetrics.getDensity(). That
        // method is absent on some Android 14 builds, crashing at launch with NoSuchMethodError.
        val configuration = LocalConfiguration.current
        val windowSizeClass = WindowSizeClass.calculateFromSize(
          DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp),
        )
        DatadogViewTrackingEffect(backstackController)
        HedvigApp(
          backstackController = backstackController,
          deepLinkReadySignal = sessionReconciler.isReady,
          windowSizeClass = windowSizeClass,
          settingsDataStore = settingsDataStore,
          featureManager = featureManager,
          authTokenService = authTokenService,
          demoManager = demoManager,
          memberIdService = memberIdService,
          deepLinkMatcher = deepLinkMatcher,
          imageLoader = imageLoader,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          waitUntilAppReviewDialogShouldBeOpenedUseCase = waitUntilAppReviewDialogShouldBeOpenedUseCase,
          appHost = androidAppHost,
          externalNavigator = externalNavigator,
          logoutUseCase = logoutUseCase,
          getMemberAuthorizationCodeUseCase = getMemberAuthorizationCodeUseCase,
          paymentsNotificationBadgeService = paymentsNotificationBadgeService,
          currentDestinationHolder = currentDestinationHolder,
          eventTrackingClient = eventTrackingClient,
          screenParameterExtractor = screenParameterExtractor,
          onboardingGate = navRetainedViewModel.onboardingGate,
          resetOnboardingSeenUseCase = resetOnboardingSeenUseCase,
          hostEntries = { context -> androidHostEntries(context) },
        )
      }
    }
  }

  private fun handleDeepLinkIntent(intent: Intent) {
    if (intent.action != Intent.ACTION_VIEW) return
    val uri = intent.data?.toString() ?: return
    deepLinkChannel.trySend(ExternalDeepLink(uri, intent.flags))
  }
}

/**
 * Reports the top of the back stack as a Datadog RUM view, so RUM attributes errors, resources and
 * view durations to a screen rather than to the hosting Activity.
 *
 * The wrapper is what keeps the back stack read inside its own restart scope: [Navigation3TrackingEffect]
 * is non-restartable, so calling it straight from `setContent` would attribute the read there and
 * recompose the whole app shell on every navigation.
 */
@OptIn(ExperimentalTrackingApi::class)
@Composable
private fun DatadogViewTrackingEffect(backstackController: BackstackController) {
  Navigation3TrackingEffect(
    backStack = backstackController.entries,
    keyPredicate = AnalyticsNamePredicate,
  )
}

/**
 * Logcat tag for the per-launch navigation breadcrumb emitted in [MainActivity.onCreate]. Grep this to
 * see how each launch arrived (action/flags/isTaskRoot) when diagnosing task/back-stack oddities.
 */
internal const val DEEP_LINK_STACK_DEBUG_TAG = "DeepLinkStackDebug"

private data class ExternalDeepLink(val uri: String, val launchFlags: Int)

/**
 * Whether a deep link reached this Activity inside another app's task, given the task-root position
 * and the [launchFlags] of the intent that delivered it. Such a link lands alone, so Back returns
 * straight to that app; everything else is our own task and lands on its synthetic ancestry.
 *
 * Both conditions must hold to be foreign-hosted:
 *  - **not the task root** (`!isTaskRoot`): some other activity sits below us in the task; and
 *  - **launched without [Intent.FLAG_ACTIVITY_NEW_TASK]**: Android's default for that is to place us in
 *    the *caller's* task rather than one of our own, so we joined whoever started us.
 *
 * `NEW_TASK` being set means the system gave us our own task (a fresh one, or an existing one brought
 * forward), so we are not foreign-hosted even when not its root, as with a notification tap that
 * fronted our task.
 */
internal fun isHostedInForeignTask(isTaskRoot: Boolean, launchFlags: Int): Boolean =
  !isTaskRoot && (launchFlags and Intent.FLAG_ACTIVITY_NEW_TASK) == 0

/**
 * Applies the theme in two ways:
 * 1. Uses UiModeManager to persist the last theme selected so that on new launches the splash screen matches the theme
 * 2. Uses AppCompatDelegate to set the underlying Configuration.uiMode to the theme selected
 */
private fun applyTheme(theme: Theme?, uiModeManager: UiModeManager?) {
  when (theme) {
    Theme.LIGHT -> {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
      }
    }

    Theme.DARK -> {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
      }
    }

    else -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
      } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        uiModeManager?.setApplicationNightMode(MODE_NIGHT_CUSTOM)
      }
    }
  }
}

/**
 * Android string resources otherwise resolve to Android's own pick from the phone's languages, which is another
 * language than [appLanguage] when the phone's first language is one the app does not have, Swedish for
 * [Deutsch, Svenska].
 */
@Composable
private fun rememberAppLanguageResources(appLanguage: Language): Resources {
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  return remember(context, configuration, appLanguage) { context.withAppLanguage(appLanguage).resources }
}
