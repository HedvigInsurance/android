package com.hedvig.android.feature.profile.tab

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.profile.aboutapp.AboutAppDestination
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.aboutapp.LicensesDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.myinfo.MyInfoDestination
import com.hedvig.android.feature.profile.myinfo.MyInfoViewModel
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.profileGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToPaymentInfo: (NavBackStackEntry) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (navBackStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<TopLevelGraph.PROFILE>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Profile>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.profile },
    ),
  ) {
    composable<AppDestination.TopLevelDestination.Profile>(
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
          navigateToPaymentInfo(backStackEntry)
        },
        navigateToConnectPayment = navigateToConnectPayment,
        navigateToAddMissingInfo = { contractId ->
          navigateToAddMissingInfo(backStackEntry, contractId)
        },
        openAppSettings = openAppSettings,
        openUrl = openUrl,
        viewModel = viewModel,
      )
    }
    composable<AppDestination.Eurobonus>(
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
    composable<AppDestination.MyInfo> {
      val viewModel: MyInfoViewModel = koinViewModel()
      MyInfoDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    composable<AppDestination.AboutApp> { backStackEntry ->
      val viewModel: AboutAppViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = {
          with(navigator) { backStackEntry.navigate(AppDestination.Licenses) }
        },
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
    composable<AppDestination.Licenses> {
      LicensesDestination(
        onBackPressed = navigator::navigateUp,
      )
    }
    composable<AppDestination.Settings> {
      val viewModel: SettingsViewModel = koinViewModel()
      SettingsDestination(
        viewModel = viewModel,
        openAppSettings = openAppSettings,
        navigateUp = navigator::navigateUp,
      )
    }
    nestedGraphs()
  }
}
