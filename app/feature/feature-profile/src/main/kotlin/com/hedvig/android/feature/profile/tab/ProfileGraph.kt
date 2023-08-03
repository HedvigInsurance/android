package com.hedvig.android.feature.profile.tab

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.profile.aboutapp.AboutAppDestination
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.aboutapp.LicensesDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.myinfo.MyInfoDestination
import com.hedvig.android.feature.profile.myinfo.MyInfoViewModel
import com.hedvig.android.feature.profile.payment.PaymentDestination
import com.hedvig.android.feature.profile.payment.PaymentViewModel
import com.hedvig.android.feature.profile.payment.history.PaymentHistoryDestination
import com.hedvig.android.feature.profile.payment.history.PaymentHistoryViewModel
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.profileGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  appVersionCode: String,
  isProduction: Boolean,
  navigateToPayoutScreen: () -> Unit,
  navigateToPayinScreen: () -> Unit,
  market: Market?,
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
      val viewModel: AboutAppViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = {
          with(navigator) { backStackEntry.navigate(AppDestination.Licenses) }
        },
        hedvigBuildConstants = hedvigBuildConstants,
        appVersionCode = appVersionCode,
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
    animatedComposable<AppDestination.PaymentInfo> { backStackEntry ->
      val viewModel: PaymentViewModel = koinViewModel()
      PaymentDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        onPaymentHistoryClicked = {
          with(navigator) { backStackEntry.navigate(AppDestination.PaymentHistory) }
        },
        onConnectPayoutMethod = navigateToPayoutScreen,
        onChangeBankAccount = navigateToPayinScreen,
        market = market,
      )
    }
    animatedComposable<AppDestination.PaymentHistory> {
      val viewModel: PaymentHistoryViewModel = koinViewModel()
      PaymentHistoryDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
      )
    }
    nestedGraphs()
  }
}
