package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
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
  navigateToConnectPayment: () -> Unit,
  navigateToContactInfo: (NavBackStackEntry) -> Unit,
  navigateToMissingInfo: (NavBackStackEntry, String) -> Unit,
  navigateToHelpCenter: (NavBackStackEntry) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<HomeDestination.Graph>(
    startDestination = HomeDestination.Home::class,
  ) {
    navdestination<HomeDestination.Home>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.home),
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
        navigateToConnectPayment = navigateToConnectPayment,
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
        navigateToContactInfo = {
          navigateToContactInfo(backStackEntry)
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
