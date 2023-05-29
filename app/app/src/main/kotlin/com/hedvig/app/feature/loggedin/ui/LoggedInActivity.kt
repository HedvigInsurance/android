package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import com.hedvig.android.app.navigation.HedvigNavHost
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.app.ui.rememberHedvigAppState
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.showReviewDialog
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoggedInActivity : AppCompatActivity() {
  private val welcomeViewModel: WelcomeViewModel by viewModel()
  private val reviewDialogViewModel: ReviewDialogViewModel by viewModel()

  val tabNotificationBadgeService: TabNotificationBadgeService by inject()
  val marketManager: MarketManager by inject()
  val imageLoader: ImageLoader by inject()
  val featureManager: FeatureManager by inject()
  val hAnalytics: HAnalytics by inject()
  val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    if (intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)) {
      fetchAndShowWelcomeDialog()
    }
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        reviewDialogViewModel.shouldOpenReviewDialog.collect { shouldOpenReviewDialog ->
          if (shouldOpenReviewDialog) {
            showReviewWithDelay()
          }
        }
      }
    }

    if (intent.getBooleanExtra(SHOW_RATING_DIALOG, false)) {
      lifecycleScope.launch {
        showReviewWithDelay()
      }
    }

    // todo use initialTab and clear it immediatelly, then trigger an initial navigation event
    val initialTab: TopLevelDestination? = intent.extras?.getString(INITIAL_TAB)?.let {
      TopLevelDestination.fromName(it)
    }

    setContent {
      HedvigTheme {
        HedvigApp(
          windowSizeClass = calculateWindowSizeClass(this),
          tabNotificationBadgeService = tabNotificationBadgeService,
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

  private fun fetchAndShowWelcomeDialog() {
    welcomeViewModel.fetch()
    welcomeViewModel.data.observe(this@LoggedInActivity) { data ->
      WelcomeDialog.newInstance(
        data.welcome.mapIndexed { index, page ->
          DismissiblePagerModel.TitlePage(
            ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
            page.title,
            page.paragraph,
            getString(
              if (index == data.welcome.size - 1) {
                hedvig.resources.R.string.NEWS_DISMISS
              } else {
                hedvig.resources.R.string.NEWS_PROCEED
              },
            ),
          )
        },
      )
        .show(supportFragmentManager, WelcomeDialog.TAG)
    }
    intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
  }

  private suspend fun showReviewWithDelay() {
    delay(REVIEW_DIALOG_DELAY_MILLIS)
    showReviewDialog()
  }

  companion object {
    const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"

    private const val INITIAL_TAB = "INITIAL_TAB"
    private const val SHOW_RATING_DIALOG = "SHOW_RATING_DIALOG"
    private const val REVIEW_DIALOG_DELAY_MILLIS = 2000L

    fun newInstance(
      context: Context,
      withoutHistory: Boolean = false,
      initialTab: TopLevelDestination = TopLevelDestination.HOME,
      isFromOnboarding: Boolean = false,
      showRatingDialog: Boolean = false,
    ) = Intent(context, LoggedInActivity::class.java).apply {
      if (withoutHistory) {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
      putExtra(INITIAL_TAB, initialTab.toName())
      putExtra(EXTRA_IS_FROM_ONBOARDING, isFromOnboarding)
      putExtra(SHOW_RATING_DIALOG, showRatingDialog)
    }
  }
}

@Composable
private fun HedvigApp(
  windowSizeClass: WindowSizeClass,
  tabNotificationBadgeService: TabNotificationBadgeService,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  hedvigAppState: HedvigAppState = rememberHedvigAppState(
    windowSizeClass = windowSizeClass,
    tabNotificationBadgeService = tabNotificationBadgeService,
    featureManager = featureManager,
    hAnalytics = hAnalytics,
  ),
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column {
      val backgroundColors = hedvigAppState.backgroundColors
      HedvigNavHost(
        hedvigAppState = hedvigAppState,
        marketManager = marketManager,
        imageLoader = imageLoader,
        featureManager = featureManager,
        hAnalytics = hAnalytics,
        fragmentManager = fragmentManager,
        languageService = languageService,
        modifier = Modifier
          .weight(1f)
          .then(
            if (hedvigAppState.shouldShowBottomBar) {
              Modifier.consumeWindowInsets(NavigationBarDefaults.windowInsets)
            } else {
              Modifier
            },
          )
          .then(
            if (backgroundColors != null) {
              Modifier.drawWithCache {
                val gradient = Brush.linearGradient(backgroundColors)
                onDrawBehind {
                  drawRect(gradient)
                }
              }
            } else {
              Modifier
            },
          ),
      )
      AnimatedVisibility(
        visible = hedvigAppState.shouldShowBottomBar,
        enter = expandVertically(clip = false),
        exit = shrinkVertically(clip = false),
      ) {
        val topLevelDestinations by hedvigAppState.topLevelDestinations.collectAsStateWithLifecycle()
        val destinationsWithNotifications by hedvigAppState
          .topLevelDestinationsWithNotifications.collectAsStateWithLifecycle()
        HedvigBottomBar(
          destinations = topLevelDestinations,
          destinationsWithNotifications = destinationsWithNotifications,
          onNavigateToDestination = hedvigAppState::navigateToTopLevelDestination,
          currentDestination = hedvigAppState.currentDestination,
        )
      }
    }
  }
}
