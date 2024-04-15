package com.hedvig.android.app

import android.app.Activity
import android.app.UiModeManager
import android.app.UiModeManager.MODE_NIGHT_CUSTOM
import android.app.assist.AssistContent
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import arrow.fx.coroutines.raceN
import coil.ImageLoader
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.app.ui.HedvigApp
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.language.LanguageAndMarketLaunchCheckUseCase
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.allDeepLinkUriPatterns
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import com.stylianosgakis.navigation.recents.url.sharing.provideAssistContent
import hedvig.resources.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
  private val applicationScope: ApplicationScope by inject()
  private val authTokenService: AuthTokenService by inject()
  private val demoManager: DemoManager by inject()
  private val featureManager: FeatureManager by inject()
  private val getOnlyHasNonPayingContractsUseCase: GetOnlyHasNonPayingContractsUseCaseProvider by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()
  private val imageLoader: ImageLoader by inject()
  private val languageService: LanguageService by inject()
  private val marketManager: MarketManager by inject()
  private val settingsDataStore: SettingsDataStore by inject()
  private val tabNotificationBadgeService: TabNotificationBadgeService by inject()
  private val waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase by inject()
  private val languageAndMarketLaunchCheckUseCase: LanguageAndMarketLaunchCheckUseCase by inject()
  private val activityNavigator: ActivityNavigator by inject()

  private var navController: NavController? = null

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
    applicationScope.launch {
      languageAndMarketLaunchCheckUseCase.invoke()
    }
    val uiModeManager = getSystemService<UiModeManager>()
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        settingsDataStore.observeTheme().collectLatest { theme ->
          applyTheme(theme, uiModeManager)
        }
      }
    }
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        if (showSplash.value == false) return@repeatOnLifecycle
        raceN(
          { authTokenService.authStatus.first { it != null } },
          { demoManager.isDemoMode().first { it == true } },
        )
        showSplash.update { false }
      }
    }

    setContent {
      val windowSizeClass = calculateWindowSizeClass(this)
      val navHostController = rememberNavController().also { navController = it }
      LifecycleStartEffect(navHostController) {
        navController = navHostController
        onStopOrDispose {
          navController = null
        }
      }
      HedvigApp(
        navHostController = navHostController,
        tabNotificationBadgeService = tabNotificationBadgeService,
        settingsDataStore = settingsDataStore,
        getOnlyHasNonPayingContractsUseCase = getOnlyHasNonPayingContractsUseCase,
        featureManager = featureManager,
        splashIsRemovedSignal = splashIsRemovedSignal,
        activityNavigator = activityNavigator,
        authTokenService = authTokenService,
        demoManager = demoManager,
        hedvigDeepLinkContainer = hedvigDeepLinkContainer,
        marketManager = marketManager,
        imageLoader = imageLoader,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
        waitUntilAppReviewDialogShouldBeOpenedUseCase = waitUntilAppReviewDialogShouldBeOpenedUseCase,
        enableEdgeToEdge = { systemBarStyle ->
          enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
          )
        },
        shouldShowRequestPermissionRationale = ::shouldShowRequestPermissionRationale,
        goToPlayStore = { activityNavigator.tryOpenPlayStore(this) },
        openEmailApp = ::openEmailApp,
        finishApp = ::finish,
        tryShowAppStoreReviewDialog = ::tryShowAppStoreReviewDialog,
        windowSizeClass = windowSizeClass,
      )
    }
  }

  override fun onProvideAssistContent(outContent: AssistContent) {
    super.onProvideAssistContent(outContent)
    navController?.provideAssistContent(outContent, hedvigDeepLinkContainer.allDeepLinkUriPatterns)
    outContent.webUri?.let {
      logcat { "Providing a deep link to current screen: $it" }
    }
  }

  private fun openEmailApp() {
    openEmail(getString(R.string.login_bottom_sheet_view_code))
  }

  companion object {
    fun newInstance(context: Context, withoutHistory: Boolean = false): Intent =
      Intent(context, MainActivity::class.java).apply {
        logcat(LogPriority.INFO) { "LoggedInActivity.newInstance was called. withoutHistory:$withoutHistory" }
        if (withoutHistory) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
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

private fun Activity.tryShowAppStoreReviewDialog() {
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
        manager.launchReviewFlow(this@tryShowAppStoreReviewDialog, reviewInfo).apply {
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

private fun Activity.openEmail(title: String) {
  val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))

  val resInfo = packageManager.queryIntentActivities(emailIntent, 0)
  if (resInfo.isNotEmpty()) {
    // First create an intent with only the package name of the first registered email app
    // and build a picked based on it
    val intentChooser = packageManager.getLaunchIntentForPackage(
      resInfo.first().activityInfo.packageName,
    )
    val openInChooser = Intent.createChooser(intentChooser, title)

    try {
      // Then create a list of LabeledIntent for the rest of the registered email apps and add to the picker selection
      val emailApps = resInfo.toLabeledIntentArray(packageManager)
      openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailApps)
    } catch (_: NullPointerException) {
      // OnePlus crash prevention. Simply go with the initial email app found, don't give more options.
      // console.firebase.google.com/u/0/project/hedvig-app/crashlytics/app/android:com.hedvig.app/issues/06823149a4ff8a411f4508e0cbfae9f4
    }

    startActivity(openInChooser)
  } else {
    logcat(LogPriority.ERROR) { "No email app found" }
  }
}

private fun List<ResolveInfo>.toLabeledIntentArray(packageManager: PackageManager): Array<LabeledIntent> = map {
  val packageName = it.activityInfo.packageName
  val intent = packageManager.getLaunchIntentForPackage(packageName)
  LabeledIntent(intent, packageName, it.loadLabel(packageManager), it.icon)
}.toTypedArray()
