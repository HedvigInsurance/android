package com.hedvig.android.app

import android.app.Activity
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
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.ImageLoader
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.externalnavigator.ExternalNavigatorImpl
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.navigation.CurrentDestinationHolder
import com.hedvig.android.app.navigation.SessionReconciler
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.rive.RiveInitializer
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageLaunchCheckUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.notification.badge.data.payment.MissedPaymentNotificationService
import com.hedvig.android.theme.Theme
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import java.util.Locale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
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
  private lateinit var getOnlyHasNonPayingContractsUseCase: Provider<GetOnlyHasNonPayingContractsUseCase>

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
  private lateinit var languageLaunchCheckUseCase: LanguageLaunchCheckUseCase

  @Inject
  private lateinit var logoutUseCase: LogoutUseCase

  @Inject
  private lateinit var getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase

  @Inject
  private lateinit var memberIdService: MemberIdService

  @Inject
  private lateinit var missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>

  @Inject
  private lateinit var currentDestinationHolder: CurrentDestinationHolder

  @Inject
  private lateinit var sessionReconciler: SessionReconciler

  // The single app-scoped backstack controller. Injected here to seed/restore it, attach the
  // Activity-bound task hooks, and hand it to HedvigApp/NavDisplay; the same singleton is exposed to
  // Presenters as a plain Backstack.
  @Inject
  private lateinit var backstackController: BackstackController

  @Inject
  private lateinit var serializersModules: Set<SerializersModule>

  /**
   * External/notification VIEW intents are forwarded here as raw URI strings. [HedvigApp] collects them and routes
   * each through the in-app deep-link matcher once the member is logged in. Replaces Nav2's automatic launch-intent
   * deep-link handling on the (now removed) NavController.
   */
  private val deepLinkChannel = Channel<String>(Channel.UNLIMITED)

  /**
   * A channel to report whenever the splash screen has stopped showing. This is used to let `enableEdgeToEdge` be run
   * again, this time properly taking into account the current background color, and not whatever might have been
   * showing in the splash screen itself. Without this, `enableEdgeToEdge` might try to compensate for the background
   * color if it turns out to be different from what the app background color is, since the night/day theme may have
   * been overwritten from the in-app settings.
   */
  private val splashIsRemovedSignal = Channel<Unit>(Channel.UNLIMITED)

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
    val defaultLocale = getSystemLocale(resources.configuration)
    languageLaunchCheckUseCase.invoke(defaultLocale)
    val uiModeManager = getSystemService<UiModeManager>()
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        settingsDataStore.observeTheme().collectLatest { theme ->
          applyTheme(theme, uiModeManager)
        }
      }
    }
    if (savedInstanceState == null) {
      handleDeepLinkIntent(intent)
    }
    addOnNewIntentListener { newIntent -> handleDeepLinkIntent(newIntent) }

    val externalNavigator = ExternalNavigatorImpl(this, hedvigBuildConstants.appPackageId)
    val androidAppHost = object : AndroidAppHost {
      override fun finishApp() = finish()

      override fun applyEdgeToEdgeStyle(systemBarStyle: SystemBarStyle) {
        enableEdgeToEdge(
          statusBarStyle = systemBarStyle,
          navigationBarStyle = systemBarStyle,
        )
      }

      override fun shouldShowPermissionRationale(permission: String): Boolean =
        shouldShowRequestPermissionRationale(permission)

      override fun tryShowAppStoreReviewDialog() = tryShowPlayStoreReviewDialog()
    }
    RiveInitializer.init(this)
    // Attach the Activity-bound task hooks to the app-scoped controller. Done here (not at
    // construction) and re-attached on every recreation: the singleton outlives any single Activity,
    // so capturing an Activity-bound lambda in the constructor would be the stale-reference leak we
    // set out to avoid.
    backstackController.isOwnTask = { isTaskRoot }
    backstackController.escapeToOwnTask = { parentStack ->
      NavigationStateBridge.escapeToOwnTask(this@MainActivity, parentStack, serializersModules)
    }
    NavigationStateBridge.restoreAndPersist(
      backstackController = backstackController,
      savedStateRegistry = savedStateRegistry,
      intent = intent,
      isColdStart = savedInstanceState == null,
      serializersModules = serializersModules,
    )
    lifecycleScope.launch {
      sessionReconciler.reconcile()
      sessionReconciler.observeForcedLogout(lifecycle)
    }
    setContent {
      CompositionLocalProvider(
        LocalMetroViewModelFactory provides (application as HedvigApplication).appGraph.metroViewModelFactory,
      ) {
        val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
        HedvigApp(
          backstackController = backstackController,
          deepLinkChannel = deepLinkChannel,
          windowSizeClass = windowSizeClass,
          settingsDataStore = settingsDataStore,
          getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
          featureManager = featureManager,
          splashIsRemovedSignal = splashIsRemovedSignal,
          authTokenService = authTokenService,
          demoManager = demoManager,
          memberIdService = memberIdService,
          deepLinkMatcher = deepLinkMatcher,
          imageLoader = imageLoader,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          waitUntilAppReviewDialogShouldBeOpenedUseCase = waitUntilAppReviewDialogShouldBeOpenedUseCase,
          androidAppHost = androidAppHost,
          externalNavigator = externalNavigator,
          logoutUseCase = logoutUseCase,
          getMemberAuthorizationCodeUseCase = getMemberAuthorizationCodeUseCase,
          missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
          currentDestinationHolder = currentDestinationHolder,
        )
      }
    }
  }

  private fun handleDeepLinkIntent(intent: Intent) {
    if (intent.action != Intent.ACTION_VIEW) return
    val uri = intent.data?.toString() ?: return
    logcat { "MainActivity received deep-link intent for uri:$uri" }
    deepLinkChannel.trySend(uri)
  }
}

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

private fun Activity.tryShowPlayStoreReviewDialog() {
  val tag = "PlayStoreReview"
  val manager = ReviewManagerFactory.create(this)
  logcat(LogPriority.INFO) { "$tag: requestReviewFlow" }
  manager.requestReviewFlow().apply {
    addOnFailureListener { logcat(LogPriority.INFO, it) { "$tag: requestReviewFlow failed:${it.message}" } }
    addOnCanceledListener { logcat(LogPriority.INFO) { "$tag: requestReviewFlow cancelled" } }
    addOnCompleteListener { task ->
      if (task.isSuccessful) {
        logcat(LogPriority.INFO) { "$tag: requestReviewFlow completed" }
        val reviewInfo = task.result
        logcat(LogPriority.INFO) { "$tag: launchReviewFlow with ReviewInfo:$reviewInfo" }
        manager.launchReviewFlow(this@tryShowPlayStoreReviewDialog, reviewInfo).apply {
          addOnFailureListener { logcat(LogPriority.INFO, it) { "$tag: launchReviewFlow failed:${it.message}" } }
          addOnCanceledListener { logcat(LogPriority.INFO) { "$tag: launchReviewFlow canceled" } }
          addOnCompleteListener { logcat(LogPriority.INFO) { "$tag: launchReviewFlow completed" } }
        }
      } else {
        val exception = task.exception
        val errorMessage = if (exception != null && exception is ReviewException) {
          "ReviewException:${exception.message}. ReviewException::errorCode:${exception.errorCode}"
        } else {
          "Unknown error with message: ${exception?.message}"
        }
        logcat(LogPriority.INFO, exception) { "$tag: requestReviewFlow failed. Error:$errorMessage" }
      }
    }
  }
}

private fun getSystemLocale(config: android.content.res.Configuration): Locale {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Resources.getSystem().configuration.locales[0]
  } else {
    @Suppress("DEPRECATION")
    config.locale
  }
}
