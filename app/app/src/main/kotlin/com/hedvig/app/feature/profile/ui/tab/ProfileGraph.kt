package com.hedvig.app.feature.profile.ui.tab

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.businessmodel.businessModelGraph
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusDestination
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusViewModel
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.profileGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  windowSizeClass: WindowSizeClass,
) {
  animatedNavigation<TopLevelGraph.PROFILE>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Profile>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.profile },
    ),
  ) {
    animatedComposable<AppDestination.TopLevelDestination.Profile>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: ProfileViewModel = koinViewModel()
      ProfileDestination(
        navigateToEurobonus = {
          with(navigator) { backStackEntry.navigate(AppDestination.Eurobonus) }
        },
        navigateToBusinessModel = {
          with(navigator) { backStackEntry.navigate(AppDestination.BusinessModel) }
        },
        viewModel = viewModel,
      )
    }
    animatedComposable<AppDestination.Eurobonus>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.eurobonus },
      ),
    ) {
      val viewModel: EurobonusViewModel = koinViewModel()
      EurobonusDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    businessModelGraph(
      navigator = navigator,
      windowSizeClass = windowSizeClass,
    )
  }
}
