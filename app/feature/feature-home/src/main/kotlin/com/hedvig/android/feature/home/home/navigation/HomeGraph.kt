package com.hedvig.android.feature.home.home.navigation

import android.content.Intent
import androidx.core.os.BundleCompat
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.coinsured.CoInsuredFlowType
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
  navigateToClaimDetails: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
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
      // The `/submit-claim` deep link lands on Home but carries no route argument, so the typed `startClaimFlow`
      // resolves to its default (false). Detect that entry point from the launching deep link intent instead, so the
      // public URL stays clean while still auto-opening the start-claim consent sheet.
      val startClaimFlow = startClaimFlow || it.wasLaunchedFromClaimDeepLink(hedvigDeepLinkContainer)
      HomeDestination(
        viewModel = viewModel,
        startClaimFlow = startClaimFlow,
        onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        navigateToClaimChat = dropUnlessResumed { navigateToClaimChat() },
        navigateToClaimChatInDevMode = dropUnlessResumed { navigateToClaimChatInDevMode() },
        onClaimDetailCardClicked = dropUnlessResumed { claimId: String ->
          navigateToClaimDetails(claimId)
        },
        navigateToConnectPayment = dropUnlessResumed { navigateToConnectPayment() },
        navigateToConnectPayout = dropUnlessResumed { navigateToConnectPayout() },
        navigateToMissingInfo = dropUnlessResumed { contractId, type -> navigateToMissingInfo(contractId, type) },
        navigateToHelpCenter = dropUnlessResumed { navigateToHelpCenter() },
        openUrl = openUrl,
        openCrossSellUrl = openCrossSellUrl,
        openAppSettings = openAppSettings,
        navigateToFirstVet = dropUnlessResumed { sections ->
          navController.navigate(HomeDestination.FirstVet(sections))
        },
        navigateToContactInfo = dropUnlessResumed {
          navigateToContactInfo()
        },
        imageLoader = imageLoader,
        navigateToChipId = navigateToChipIdScreen,
      )
    }
    navdestination<HomeDestination.FirstVet>(
      HomeDestination.FirstVet,
    ) {
      FirstVetDestination(
        sections,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        openUrl = openUrl,
      )
    }
    nestedGraphs()
  }
}

/**
 * Whether this back stack entry was opened by the claim flow deep link (`/submit-claim`). The deep link does not carry
 * any route argument, so we inspect the launching intent's URI and match it against the known claim flow patterns.
 */
private fun NavBackStackEntry.wasLaunchedFromClaimDeepLink(hedvigDeepLinkContainer: HedvigDeepLinkContainer): Boolean {
  val arguments = arguments ?: return false
  val deepLinkIntent = BundleCompat.getParcelable(arguments, NavController.KEY_DEEP_LINK_INTENT, Intent::class.java)
  val deepLinkUri = deepLinkIntent?.data?.toString() ?: return false
  return hedvigDeepLinkContainer.claimFlow.any { claimFlowUri -> deepLinkUri.startsWith(claimFlowUri) }
}
