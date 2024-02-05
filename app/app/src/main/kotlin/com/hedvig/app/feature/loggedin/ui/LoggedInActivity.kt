package com.hedvig.app.feature.loggedin.ui

import android.app.UiModeManager
import android.app.UiModeManager.MODE_NIGHT_CUSTOM
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arrow.fx.coroutines.raceN
import coil.ImageLoader
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.app.ui.rememberHedvigAppState
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoggedInActivity : AppCompatActivity() {
  private val reviewDialogViewModel: ReviewDialogViewModel by viewModel()

  private val authTokenService: AuthTokenService by inject()
  private val demoManager: DemoManager by inject()
  private val tabNotificationBadgeService: TabNotificationBadgeService by inject()
  private val marketManager: MarketManager by inject()
  private val imageLoader: ImageLoader by inject()
  private val featureManager: FeatureManager by inject()
  private val languageService: LanguageService by inject()
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()
  private val settingsDataStore: SettingsDataStore by inject()
  private val getOnlyHasNonPayingContractsUseCase: GetOnlyHasNonPayingContractsUseCaseProvider by inject()

  private val activityNavigator: ActivityNavigator by inject()

  // Shows the splash screen as long as the auth status or the demo mode status is still undetermined
  private val showSplash = MutableStateFlow(true)

  /**
   * A channel to report whenever the splash screen has stopped showing. This is used to let `enableEdgeToEdge` be run
   * again, this time properly taking into account the current background color, and not whatever might have been
   * showing in the splash screen itself. Without this, `enableEdgeToEdge` might try to compensate for the background
   * color if it turns out to be different from what the app background color is, since the night/day theme may have
   * been overwritten from the in-app settings.
   */
  private val splashIsRemovedSignal = Channel<Unit>(Channel.UNLIMITED)

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().apply {
      setKeepOnScreenCondition { showSplash.value == true }
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
    super.onCreate(savedInstanceState)
    val uiModeManager = getSystemService<UiModeManager>()

    val intent: Intent = intent
    lifecycleScope.launch {
      launch {
        featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY).collectLatest {
          if (it) {
            applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
            finish()
            cancel()
          }
        }
      }
      launch {
        settingsDataStore.observeTheme().collectLatest { theme ->
          applyTheme(theme, uiModeManager)
        }
      }
      launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
          raceN(
            { authTokenService.authStatus.first { it != null } },
            { demoManager.isDemoMode().first { it == true } },
          )
          showSplash.update { false }
        }
      }
      launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
          authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
          reviewDialogViewModel.shouldOpenReviewDialog.collect { shouldOpenReviewDialog ->
            if (shouldOpenReviewDialog) {
              showReviewWithDelay()
            }
          }
        }
      }
      if (intent.getBooleanExtra(SHOW_RATING_DIALOG, false)) {
        launch {
          authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
          showReviewWithDelay()
        }
      }
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        authTokenService.authStatus
          .onEach { authStatus ->
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
          .filterIsInstance<AuthStatus.LoggedOut>()
          .first()
        // Wait for demo mode to evaluate to false to know that we must leave the activity
        demoManager.isDemoMode().first { it == false }
        activityNavigator.navigateToMarketingActivity()
        finish()
      }
    }

    setContent {
      val market by marketManager.market.collectAsStateWithLifecycle()
      val windowSizeClass = calculateWindowSizeClass(this)
      val hedvigAppState = rememberHedvigAppState(
        windowSizeClass = windowSizeClass,
        tabNotificationBadgeService = tabNotificationBadgeService,
        settingsDataStore = settingsDataStore,
        getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
      )
      val darkTheme = hedvigAppState.darkTheme
      EnableEdgeToEdgeSideEffect(darkTheme)
      HedvigTheme(darkTheme = darkTheme) {
        HedvigApp(
          hedvigAppState = hedvigAppState,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          activityNavigator = activityNavigator,
          getInitialTab = {
            intent.extras?.getString(INITIAL_TAB)?.let {
              TopLevelGraph.fromName(it)
            }
          },
          clearInitialTab = {
            intent.removeExtra(INITIAL_TAB)
          },
          shouldShowRequestPermissionRationale = ::shouldShowRequestPermissionRationale,
          market = market,
          imageLoader = imageLoader,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
        )
      }
    }
  }

  @Composable
  private fun EnableEdgeToEdgeSideEffect(darkTheme: Boolean) {
    val splashIsRemovedIndex by produceState(0) {
      splashIsRemovedSignal.receiveAsFlow().collectLatest { value = value + 1 }
    }
    DisposableEffect(darkTheme, splashIsRemovedIndex) {
      enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
      )
      onDispose {}
    }
  }

  private suspend fun showReviewWithDelay() {
    delay(REVIEW_DIALOG_DELAY_MILLIS)
    val manager = ReviewManagerFactory.create(this)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val reviewInfo = task.result
        manager.launchReviewFlow(this, reviewInfo)
      }
    }
  }

  companion object {
    private const val INITIAL_TAB = "INITIAL_TAB"
    private const val SHOW_RATING_DIALOG = "SHOW_RATING_DIALOG"
    private const val REVIEW_DIALOG_DELAY_MILLIS = 2000L

    fun newInstance(
      context: Context,
      withoutHistory: Boolean = false,
      initialTab: TopLevelGraph = TopLevelGraph.HOME,
      showRatingDialog: Boolean = false,
    ): Intent = Intent(context, LoggedInActivity::class.java).apply {
      logcat(LogPriority.INFO) { "LoggedInActivity.newInstance was called. withoutHistory:$withoutHistory" }
      if (withoutHistory) {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
      if (initialTab != TopLevelGraph.HOME) {
        putExtra(INITIAL_TAB, initialTab.toName())
      }
      putExtra(SHOW_RATING_DIALOG, showRatingDialog)
    }
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
