package com.hedvig.android.feature.home.home.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.home.home.ui.FirstVetDestination
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.homeEntries(
  nestedEntries: EntryProviderScope<HedvigNavKey>.() -> Unit,
  backstack: Backstack,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  navigateToClaimChat: (resumeClaim: Boolean) -> Unit,
  navigateToChipIdScreen: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  navigateToTravelCertificate: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  entry<HomeKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: HomeViewModel = metroViewModel()
    HomeDestination(
      viewModel = viewModel,
      onNavigateToInbox = dropUnlessResumed { onNavigateToInbox() },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateToClaimChat = dropUnlessResumed { resumeClaim -> navigateToClaimChat(resumeClaim) },
      onClaimDetailCardClicked = dropUnlessResumed { claimId: String ->
        navigateToClaimDetails(claimId)
      },
      navigateToConnectPayment = dropUnlessResumed { navigateToConnectPayment() },
      navigateToConnectPayout = dropUnlessResumed { navigateToConnectPayout() },
      navigateToMissingInfo = dropUnlessResumed { contractId, type -> navigateToMissingInfo(contractId, type) },
      navigateToHelpCenter = dropUnlessResumed { navigateToHelpCenter() },
      navigateToMovingFlow = dropUnlessResumed { navigateToMovingFlow() },
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      openAppSettings = openAppSettings,
      navigateToFirstVet = dropUnlessResumed { sections ->
        backstack.add(FirstVetKey(sections))
      },
      navigateToContactInfo = dropUnlessResumed {
        navigateToContactInfo()
      },
      imageLoader = imageLoader,
      navigateToChipId = navigateToChipIdScreen,
      navigateToTravelCertificate = dropUnlessResumed { navigateToTravelCertificate() },
      navigateToAddonPurchaseFlow = dropUnlessResumed { ids -> navigateToAddonPurchaseFlow(ids) },
    )
  }
  entry<FirstVetKey> { key ->
    FirstVetDestination(
      key.sections,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      openUrl = openUrl,
    )
  }
  nestedEntries()
}
