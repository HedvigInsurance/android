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
import com.hedvig.app.feature.embark.ui.MemberIdViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppDestination
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusDestination
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusViewModel
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoDestination
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoViewModel
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.profileGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  windowSizeClass: WindowSizeClass,
  isProduction: Boolean,
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
        navigateToMyInfo = {
          with(navigator) { backStackEntry.navigate(AppDestination.MyInfo) }
        },
        navigateToAboutApp = {
          with(navigator) { backStackEntry.navigate(AppDestination.AboutApp) }
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
    animatedComposable<AppDestination.MyInfo> {
      val viewModel: MyInfoViewModel = koinViewModel()
      MyInfoDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<AppDestination.AboutApp> {
      val viewModel: MemberIdViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        isProduction = isProduction,
      )
    }
    businessModelGraph(
      navigator = navigator,
      windowSizeClass = windowSizeClass,
    )
  }
}
