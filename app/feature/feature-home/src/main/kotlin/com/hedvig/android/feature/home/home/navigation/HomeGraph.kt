package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onStartChat: (NavBackStackEntry) -> Unit,
  onStartClaim: (NavBackStackEntry) -> Unit,
  navigateToClaimDetails: (NavBackStackEntry, claimId: String) -> Unit,
  navigateToPayinScreen: () -> Unit,
  navigateToMissingInfo: (NavBackStackEntry, String) -> Unit,
  navigateToHelpCenter: (NavBackStackEntry) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<HomeDestination.Graph>(
    startDestination = createRoutePattern<HomeDestination.Home>(),
  ) {
    composable<HomeDestination.Home>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.home },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onStartChat = { onStartChat(backStackEntry) },
        onClaimDetailCardClicked = { claimId: String ->
          navigateToClaimDetails(backStackEntry, claimId)
        },
        navigateToConnectPayment = navigateToPayinScreen,
        onStartClaim = { onStartClaim(backStackEntry) },
        navigateToMissingInfo = { contractId -> navigateToMissingInfo(backStackEntry, contractId) },
        navigateToHelpCenter = { navigateToHelpCenter(backStackEntry) },
        openUrl = openUrl,
        openAppSettings = openAppSettings,
        navigateToFirstVet = { sections ->
          with(navigator) {
            backStackEntry.navigate(HomeDestination.FirstVet(sections))
          }
        },
      )
    }
    composable<HomeDestination.FirstVet> {
      FirstVetDestination(
        sections,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
    nestedGraphs()
  }
}
