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
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestinations
import com.hedvig.android.feature.profile.navigation.SettingsDestinations
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.profileGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  settingsDestinationNestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (navBackStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  navigateToDeleteAccountFeature: (navBackStackEntry: NavBackStackEntry) -> Unit,
  openAppSettings: () -> Unit,
  openChat: (navBackStackEntry: NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<ProfileDestination.Graph>(
    startDestination = createRoutePattern<ProfileDestination.Profile>(),
  ) {
    composable<ProfileDestination.Profile>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.profile },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: ProfileViewModel = koinViewModel()
      ProfileDestination(
        navigateToEurobonus = {
          with(navigator) { backStackEntry.navigate(ProfileDestinations.Eurobonus) }
        },
        navigateToMyInfo = {
          with(navigator) { backStackEntry.navigate(ProfileDestinations.MyInfo) }
        },
        navigateToAboutApp = {
          with(navigator) { backStackEntry.navigate(ProfileDestinations.AboutApp) }
        },
        navigateToSettings = {
          with(navigator) { backStackEntry.navigate(ProfileDestinations.SettingsGraph) }
        },
        navigateToTravelCertificate = {
          with(navigator) { backStackEntry.navigate(AppDestination.TravelCertificate) }
        },
        navigateToConnectPayment = navigateToConnectPayment,
        navigateToAddMissingInfo = { contractId ->
          navigateToAddMissingInfo(backStackEntry, contractId)
        },
        openAppSettings = openAppSettings,
        openUrl = openUrl,
        viewModel = viewModel,
        openChat = {
          openChat(backStackEntry)
        },
      )
    }
    composable<ProfileDestinations.Eurobonus>(
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
    composable<ProfileDestinations.MyInfo> {
      val viewModel: MyInfoViewModel = koinViewModel()
      MyInfoDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    composable<ProfileDestinations.AboutApp> { backStackEntry ->
      val viewModel: AboutAppViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = {
          with(navigator) { backStackEntry.navigate(ProfileDestinations.Licenses) }
        },
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
    composable<ProfileDestinations.Licenses> {
      LicensesDestination(
        onBackPressed = navigator::navigateUp,
      )
    }
    navigation<ProfileDestinations.SettingsGraph>(
      startDestination = createRoutePattern<SettingsDestinations.Settings>(),
    ) {
      composable<SettingsDestinations.Settings> { backStackEntry ->
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsDestination(
          viewModel = viewModel,
          navigateUp = navigator::navigateUp,
          openAppSettings = openAppSettings,
          onNavigateToDeleteAccountFeature = { navigateToDeleteAccountFeature(backStackEntry) },
        )
      }
      settingsDestinationNestedGraphs()
    }
    nestedGraphs()
  }
}
