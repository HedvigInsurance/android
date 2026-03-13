package com.hedvig.android.feature.home.home.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navController: NavController,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToOldClaimFlow: () -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToMissingInfo: (String) -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  navgraph<HomeDestination.Graph>(
    startDestination = HomeDestination.Home::class,
  ) {
    navdestination<HomeDestination.Home>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.home, hedvigDeepLinkContainer.claimFlow),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        navigateToClaimChat = dropUnlessResumed { navigateToClaimChat() },
        navigateToClaimChatInDevMode = dropUnlessResumed { navigateToClaimChatInDevMode() },
        onClaimDetailCardClicked = dropUnlessResumed { claimId: String ->
          navigateToClaimDetails(claimId)
        },
        navigateToConnectPayment = dropUnlessResumed { navigateToConnectPayment() },
        navigateToOldClaimFlow = dropUnlessResumed { navigateToOldClaimFlow() },
        navigateToMissingInfo = dropUnlessResumed { contractId -> navigateToMissingInfo(contractId) },
        navigateToHelpCenter = dropUnlessResumed { navigateToHelpCenter() },
        openUrl = openUrl,
        openAppSettings = openAppSettings,
        navigateToFirstVet = dropUnlessResumed { sections ->
          navController.navigate(HomeDestination.FirstVet(sections))
        },
        navigateToContactInfo = dropUnlessResumed {
          navigateToContactInfo()
        },
        imageLoader = imageLoader,
      )
    }
    navdestination<HomeDestination.FirstVet>(
      HomeDestination.FirstVet,
    ) {
      FirstVetDestination(
        sections,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
      )
    }
    nestedGraphs()
  }
}
