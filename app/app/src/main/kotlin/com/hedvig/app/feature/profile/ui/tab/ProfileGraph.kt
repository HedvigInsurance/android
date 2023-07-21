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
import com.hedvig.app.feature.profile.ui.aboutapp.LicensesDestination
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusDestination
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusViewModel
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoDestination
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentDestination
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel2
import com.hedvig.app.feature.settings.SettingsDestination
import com.hedvig.app.feature.settings.SettingsViewModel
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
        navigateToSettings = {
          with(navigator) { backStackEntry.navigate(AppDestination.Settings) }
        },
        navigateToPayment = {
          with(navigator) { backStackEntry.navigate(AppDestination.PaymentInfo) }
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
    animatedComposable<AppDestination.AboutApp> { backStackEntry ->
      val viewModel: MemberIdViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = {
          with(navigator) { backStackEntry.navigate(AppDestination.Licenses) }
        },
        isProduction = isProduction,
      )
    }
    animatedComposable<AppDestination.Licenses> {
      LicensesDestination(
        onBackPressed = navigator::navigateUp,
      )
    }
    animatedComposable<AppDestination.Settings> {
      val viewModel: SettingsViewModel = koinViewModel()
      SettingsDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
      )
    }
    animatedComposable<AppDestination.PaymentInfo> {
      val viewModel: PaymentViewModel2 = koinViewModel()
      PaymentDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
      )
    }
    businessModelGraph(
      navigator = navigator,
      windowSizeClass = windowSizeClass,
    )
  }
}
