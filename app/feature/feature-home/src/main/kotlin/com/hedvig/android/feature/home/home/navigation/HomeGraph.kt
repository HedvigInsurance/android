package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
  onNavigateToInbox: (NavBackStackEntry) -> Unit,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  onStartClaim: (NavBackStackEntry) -> Unit,
  navigateToClaimDetails: (NavBackStackEntry, claimId: String) -> Unit,
  navigateToPayinScreen: () -> Unit,
  navigateToMissingInfo: (NavBackStackEntry, String) -> Unit,
  navigateToHelpCenter: (NavBackStackEntry) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<HomeDestination.Graph>(
    startDestination = HomeDestination.Home::class,
  ) {
    navdestination<HomeDestination.Home>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.home },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onNavigateToInbox = { onNavigateToInbox(backStackEntry) },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
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
    navdestination<HomeDestination.FirstVet>(
      HomeDestination.FirstVet,
    ) {
      FirstVetDestination(
        sections,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
    nestedGraphs()
  }
}
