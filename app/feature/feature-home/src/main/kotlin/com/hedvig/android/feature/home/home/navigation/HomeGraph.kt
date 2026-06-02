package com.hedvig.android.feature.home.home.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.homeGraph(
  nestedGraphs: EntryProviderScope<HedvigNavKey>.() -> Unit,
  backStack: MutableList<HedvigNavKey>,
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
  navdestination<HomeKey>(
    metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit),
  ) {
    val viewModel: HomeViewModel = metroViewModel()
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
      navigateToConnectPayout = dropUnlessResumed { navigateToConnectPayout() },
      navigateToMissingInfo = dropUnlessResumed { contractId, type -> navigateToMissingInfo(contractId, type) },
      navigateToHelpCenter = dropUnlessResumed { navigateToHelpCenter() },
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      openAppSettings = openAppSettings,
      navigateToFirstVet = dropUnlessResumed { sections ->
        backStack.add(FirstVetKey(sections))
      },
      navigateToContactInfo = dropUnlessResumed {
        navigateToContactInfo()
      },
      imageLoader = imageLoader,
      navigateToChipId = navigateToChipIdScreen,
    )
  }
  navdestination<FirstVetKey> {
    FirstVetDestination(
      sections,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
      openUrl = openUrl,
    )
  }
  nestedGraphs()
}
