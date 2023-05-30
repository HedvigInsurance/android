package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.home.ui.homeGraph
import com.hedvig.app.feature.insurance.ui.tab.insuranceGraph
import com.hedvig.app.feature.profile.ui.tab.profileGraph
import com.hedvig.app.feature.referrals.ui.tab.referralsGraph
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
  languageService: LanguageService,
  modifier: Modifier = Modifier,
) {
  val navController = hedvigAppState.navController
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<TopLevelDestination.HOME>(),
    modifier = modifier,
    enterTransition = { MotionDefaults.fadeThroughEnter },
    exitTransition = { MotionDefaults.fadeThroughExit },
  ) {
    homeGraph(
      marketManager = marketManager,
      imageLoader = imageLoader,
      featureManager = featureManager,
      hAnalytics = hAnalytics,
      fragmentManager = fragmentManager,
    )
    insuranceGraph(imageLoader = imageLoader)
    referralsGraph(languageService = languageService)
    profileGraph()
  }
}
