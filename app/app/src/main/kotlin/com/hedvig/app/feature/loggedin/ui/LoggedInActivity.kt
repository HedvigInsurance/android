package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.app.ui.GradientColors
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.app.ui.HedvigBottomBar
import com.hedvig.android.app.ui.HedvigNavRail
import com.hedvig.android.app.ui.rememberHedvigAppState
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.activity.Navigator
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
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
import slimber.log.d
import slimber.log.e
import slimber.log.i

class LoggedInActivity : AppCompatActivity() {
  private val reviewDialogViewModel: ReviewDialogViewModel by viewModel()

  private val authTokenService: AuthTokenService by inject()
  private val tabNotificationBadgeService: TabNotificationBadgeService by inject()
  private val marketManager: MarketManager by inject()
  private val imageLoader: ImageLoader by inject()
  private val featureManager: FeatureManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val languageService: LanguageService by inject()
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()

  private val navigator: Navigator by inject()

  private val showSplash = MutableStateFlow(true)

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().setKeepOnScreenCondition { showSplash.value == true }
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    // check DK login by pressing app in home screen after login
//    val isBringingToFront = intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0
//    val resetTaskIfNeeded = intent.flags and Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED != 0
//    if (isBringingToFront && resetTaskIfNeeded) {
//      // We've started the app from the home screen after it was put there by pressing the home button, and not going
//      // back from the start destination. In this case, do not override the existing backstack.
//      // And since `MainActivity` is added at the top of the backstack, we need to simply pop it out and we're good.
//      // This fixes the bug in DK where exiting the app, entering the auth info, and clicking on the app in the home
//      // screen to come back to the app fails by navigating to the marketing screen again, and failing authentication.
//      onBackPressedDispatcher.onBackPressed()
//    }

    val intent: Intent = intent
    val uri: Uri? = intent.data
    lifecycleScope.launch {
      if (featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY).also { d { "Stelios: was true? $it" } }) {
        applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
        finish()
        return@launch
      }
      if (intent.getBooleanExtra(SHOW_RATING_DIALOG, false)) {
        lifecycleScope.launch {
          showReviewWithDelay()
        }
      }
      if (uri != null) {
        val pathSegments = uri.pathSegments
        val dynamicLink: DynamicLink = when {
          pathSegments.contains("direct-debit") -> DynamicLink.DirectDebit
          pathSegments.contains("connect-payment") -> DynamicLink.DirectDebit
          pathSegments.isEmpty() -> DynamicLink.None
          else -> DynamicLink.Unknown
        }
        i { "Deep link was found:$dynamicLink, with segments: ${pathSegments.joinToString(",")}" }
        if (dynamicLink is DynamicLink.DirectDebit) {
          hAnalytics.deepLinkOpened(dynamicLink.type)
          val market = marketManager.market
          if (market == null) {
            e { "Tried to open DirectDebit deep link, but market was null. Aborting and continuing to normal flow" }
          } else {
            lifecycleScope.launch {
              this@LoggedInActivity.startActivity(
                connectPayinIntent(
                  this@LoggedInActivity,
                  featureManager.getPaymentType(),
                  market,
                  false,
                ),
              )
            }
          }
        } else {
          d { "Deep link $dynamicLink did not open some specific activity" }
        }
      }
      launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
          reviewDialogViewModel.shouldOpenReviewDialog.collect { shouldOpenReviewDialog ->
            if (shouldOpenReviewDialog) {
              showReviewWithDelay()
            }
          }
        }
      }
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        d { "Stelios: Gonna start auth check" }
        authTokenService.authStatus
          .onEach { authStatus ->
            d {
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
          .onEach { authStatus ->
            if (authStatus is AuthStatus.LoggedIn) {
              showSplash.update { false }
              if (marketManager.market != null) {
                // Upcast everyone that were logged in before Norway launch to be in the Swedish market
                marketManager.market = Market.SE
              }
            }
          }
          .filterIsInstance<AuthStatus.LoggedOut>()
          .first()
        navigator.navigateToMarketingActivity()
      }
    }

    setContent {
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
          getInitialTab = {
            intent.extras?.getString(INITIAL_TAB)?.let {
              TopLevelGraph.fromName(it)
            }
          },
          clearInitialTab = {
            intent.removeExtra(INITIAL_TAB)
          },
          marketManager = marketManager,
          imageLoader = imageLoader,
          featureManager = featureManager,
          hAnalytics = hAnalytics,
          fragmentManager = supportFragmentManager,
          languageService = languageService,
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
      if (withoutHistory) {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
      putExtra(INITIAL_TAB, initialTab.toName())
      putExtra(SHOW_RATING_DIALOG, showRatingDialog)
    }
  }
}

@Composable
private fun HedvigApp(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  getInitialTab: () -> TopLevelGraph?,
  clearInitialTab: () -> Unit,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
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
    Column(
      modifier = Modifier.drawBackgroundGradient(
        colorBehindBackgroundGradient = MaterialTheme.colorScheme.background,
        backgroundColors = hedvigAppState.backgroundColors,
      ),
    ) {
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
          marketManager = marketManager,
          imageLoader = imageLoader,
          featureManager = featureManager,
          hAnalytics = hAnalytics,
          fragmentManager = fragmentManager,
          languageService = languageService,
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .then(
              if (hedvigAppState.shouldShowBottomBar) {
                Modifier.consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
              } else if (hedvigAppState.shouldShowNavRail) {
                Modifier.consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Start))
              } else {
                Modifier
              },
            ),
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

private fun Modifier.drawBackgroundGradient(
  colorBehindBackgroundGradient: Color,
  backgroundColors: GradientColors,
): Modifier = composed {
  val color1 by animateColorAsState(
    backgroundColors.color1.compositeOver(colorBehindBackgroundGradient),
    spring(stiffness = Spring.StiffnessVeryLow),
  )
  val color2 by animateColorAsState(
    backgroundColors.color2.compositeOver(colorBehindBackgroundGradient),
    spring(stiffness = Spring.StiffnessVeryLow),
  )
  val color3 by animateColorAsState(
    backgroundColors.color3.compositeOver(colorBehindBackgroundGradient),
    spring(stiffness = Spring.StiffnessVeryLow),
  )
  Modifier.drawWithCache {
    val gradient = Brush.linearGradient(listOf(color1, color2, color3))
    onDrawBehind {
      drawRect(gradient)
    }
  }
}
