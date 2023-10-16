package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arrow.fx.coroutines.raceN
import coil.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.app.ui.HedvigBottomBar
import com.hedvig.android.app.ui.HedvigNavRail
import com.hedvig.android.app.ui.rememberHedvigAppState
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.android.theme.Theme
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import com.hedvig.app.service.DynamicLink
import com.hedvig.app.util.extensions.showReviewDialog
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
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
  private val hAnalytics: HAnalytics by inject()
  private val languageService: LanguageService by inject()
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()
  private val hedvigBuildConstants: HedvigBuildConstants by inject()
  private val settingsDataStore: SettingsDataStore by inject()

  private val activityNavigator: ActivityNavigator by inject()

  // Shows the splash screen as long as the auth status is still undetermined, that's the only condition.
  private val showSplash = MutableStateFlow(true)

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().apply {
      setKeepOnScreenCondition { showSplash.value == true }
      setOnExitAnimationListener {
        logcat(LogPriority.INFO) { "Splash screen will be removed" }
        it.remove()
      }
    }
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val intent: Intent = intent
    val uri: Uri? = intent.data
    lifecycleScope.launch {
      if (featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY)) {
        applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
        finish()
        return@launch
      }
      launch {
        settingsDataStore.observeTheme().first()?.apply()
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
      if (uri != null) {
        launch {
          lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            authTokenService.authStatus.first { it is AuthStatus.LoggedIn }
            val pathSegments = uri.pathSegments
            val dynamicLink: DynamicLink = when {
              pathSegments.contains("direct-debit") -> DynamicLink.DirectDebit
              pathSegments.contains("connect-payment") -> DynamicLink.DirectDebit
              pathSegments.isEmpty() -> DynamicLink.None
              else -> DynamicLink.Unknown
            }
            logcat(LogPriority.INFO) {
              "Deep link was found:$dynamicLink, with segments: ${pathSegments.joinToString(",")}"
            }
            if (dynamicLink is DynamicLink.DirectDebit) {
              hAnalytics.deepLinkOpened(dynamicLink.type)
              val market = marketManager.market.first()
              this@LoggedInActivity.startActivity(
                connectPayinIntent(
                  this@LoggedInActivity,
                  featureManager.getPaymentType(),
                  market,
                  false,
                ),
              )
            } else {
              logcat { "Deep link $dynamicLink did not open some specific activity" }
            }
          }
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
      HedvigTheme {
        val windowSizeClass = calculateWindowSizeClass(this)
        HedvigApp(
          hedvigAppState = rememberHedvigAppState(
            windowSizeClass = windowSizeClass,
            tabNotificationBadgeService = tabNotificationBadgeService,
            featureManager = featureManager,
            hAnalytics = hAnalytics,
          ),
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
          featureManager = featureManager,
          hAnalytics = hAnalytics,
          fragmentManager = supportFragmentManager,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
        )
      }
    }
  }

  private suspend fun showReviewWithDelay() {
    delay(REVIEW_DIALOG_DELAY_MILLIS)
    showReviewDialog()
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

@Composable
private fun HedvigApp(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  activityNavigator: ActivityNavigator,
  getInitialTab: () -> TopLevelGraph?,
  clearInitialTab: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  market: Market,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  LaunchedEffect(getInitialTab, clearInitialTab, hedvigAppState) {
    val initialTab: TopLevelGraph = getInitialTab() ?: return@LaunchedEffect
    clearInitialTab()
    hedvigAppState.navigateToTopLevelGraph(initialTab)
  }
  Surface(
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      Row(Modifier.weight(1f).fillMaxWidth()) {
        AnimatedVisibility(
          visible = hedvigAppState.shouldShowNavRail,
          enter = expandHorizontally(expandFrom = Alignment.End),
          exit = shrinkHorizontally(shrinkTowards = Alignment.End),
        ) {
          val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsStateWithLifecycle()
          val destinationsWithNotifications by hedvigAppState
            .topLevelGraphsWithNotifications.collectAsStateWithLifecycle()
          HedvigNavRail(
            destinations = topLevelGraphs,
            destinationsWithNotifications = destinationsWithNotifications,
            onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
            currentDestination = hedvigAppState.currentDestination,
          )
        }
        HedvigNavHost(
          hedvigAppState = hedvigAppState,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          activityNavigator = activityNavigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          imageLoader = imageLoader,
          market = market,
          featureManager = featureManager,
          hAnalytics = hAnalytics,
          fragmentManager = fragmentManager,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .animatedNavigationBarInsetsConsumption(hedvigAppState),
        )
      }
      AnimatedVisibility(
        visible = hedvigAppState.shouldShowBottomBar,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
      ) {
        val topLevelGraphs by hedvigAppState.topLevelGraphs.collectAsStateWithLifecycle()
        val destinationsWithNotifications by hedvigAppState
          .topLevelGraphsWithNotifications.collectAsStateWithLifecycle()
        HedvigBottomBar(
          destinations = topLevelGraphs,
          destinationsWithNotifications = destinationsWithNotifications,
          onNavigateToDestination = hedvigAppState::navigateToTopLevelGraph,
          currentDestination = hedvigAppState.currentDestination,
        )
      }
    }
  }
}

/**
 * Animates how we consume the insets, so that when we leave a screen which does not consume any insets, and we enter a
 * screen which does (by showing the bottom nav for example) then we don't want the outgoing screen to have its
 * contents snap to the bounds of the new insets immediately. This animation makes this visual effect look much more
 * fluid.
 */
@OptIn(ExperimentalLayoutApi::class)
private fun Modifier.animatedNavigationBarInsetsConsumption(
  hedvigAppState: HedvigAppState,
) = composed {
  val density = LocalDensity.current
  val insetsToConsume = if (hedvigAppState.shouldShowBottomBar) {
    WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(density)
  } else if (hedvigAppState.shouldShowNavRail) {
    WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Left).asPaddingValues(density)
  } else {
    PaddingValues(0.dp)
  }

  val paddingValuesVectorConverter: TwoWayConverter<PaddingValues, AnimationVector4D> = TwoWayConverter(
    convertToVector = { paddingValues ->
      AnimationVector4D(
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).value,
        paddingValues.calculateRightPadding(LayoutDirection.Ltr).value,
        paddingValues.calculateTopPadding().value,
        paddingValues.calculateBottomPadding().value,
      )
    },
    convertFromVector = { animationVector4d ->
      val leftPadding = animationVector4d.v1
      val rightPadding = animationVector4d.v2
      val topPadding = animationVector4d.v3
      val bottomPadding = animationVector4d.v4
      PaddingValues(
        start = leftPadding.dp,
        end = rightPadding.dp,
        top = topPadding.dp,
        bottom = bottomPadding.dp,
      )
    },
  )
  val animatedInsetsToConsume: PaddingValues by animateValueAsState(
    targetValue = insetsToConsume,
    typeConverter = paddingValuesVectorConverter,
    animationSpec = tween(MotionTokens.DurationMedium1.toInt()),
    label = "Padding values inset animation",
  )
  consumeWindowInsets(animatedInsetsToConsume)
}

/**
 * Move just to the settings place where this is edited after we remove the dark_mode feature flag
 * We do not need to set this on every app launch from that point on.
 */
private fun Theme.apply() = when (this) {
  Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
  Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
  Theme.SYSTEM_DEFAULT -> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
  }
}
