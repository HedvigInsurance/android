package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigator: Navigator,
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
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.home),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onNavigateToInbox = { onNavigateToInbox() },
        onNavigateToNewConversation = { onNavigateToNewConversation() },
        navigateToClaimChat = navigateToClaimChat,
        navigateToClaimChatInDevMode = navigateToClaimChatInDevMode,
        onClaimDetailCardClicked = { claimId: String ->
          navigateToClaimDetails(claimId)
        },
        navigateToConnectPayment = navigateToConnectPayment,
        navigateToOldClaimFlow = { navigateToOldClaimFlow() },
        navigateToMissingInfo = { contractId -> navigateToMissingInfo(contractId) },
        navigateToHelpCenter = { navigateToHelpCenter() },
        openUrl = openUrl,
        openAppSettings = openAppSettings,
        navigateToFirstVet = { sections ->
          navigator.navigate(HomeDestination.FirstVet(sections))
        },
        navigateToContactInfo = {
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
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
      )
    }
    nestedGraphs()
  }
}
