package com.hedvig.android.feature.insurances.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.feature.insurances.data.AvailableAddon
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.InsuranceDestination
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popBackstack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.insuranceGraph(
  nestedGraphs: EntryProviderScope<HedvigNavKey>.() -> Unit,
  backstack: Backstack,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  startMovingFlow: () -> Unit,
  onNavigateToStartChangeTier: (contractId: String) -> Unit,
  startTerminationFlow: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (contractId: String) -> Unit,
  startEditCoOwners: (contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (contractId: String) -> Unit,
  startEditCoOwnersAddMissingInfo: (contractId: String) -> Unit,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<ContractId>, AvailableAddon?) -> Unit,
  onNavigateToRemoveAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToUpgradeAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToChipIdScreen: (String) -> Unit,
) {
  nestedGraphs()
  entry<InsurancesKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: InsuranceViewModel = metroViewModel()
    InsuranceDestination(
      viewModel = viewModel,
      onInsuranceCardClick = dropUnlessResumed { contractId: String ->
        backstack.add(InsuranceContractDetailKey(contractId))
      },
      onCrossSellClick = dropUnlessResumed { url: String -> openCrossSellUrl(url) },
      navigateToCancelledInsurances = dropUnlessResumed {
        backstack.add(TerminatedInsurancesKey)
      },
      onNavigateToMovingFlow = dropUnlessResumed { startMovingFlow() },
      imageLoader = imageLoader,
      onNavigateToAddonPurchaseFlow = dropUnlessResumed { ids: List<ContractId> ->
        onNavigateToAddonPurchaseFlow(ids, null)
      },
    )
  }
  entry<InsuranceContractDetailKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) { key ->
    val contractDetail = key
    val viewModel: ContractDetailViewModel =
      assistedMetroViewModel<ContractDetailViewModel, ContractDetailViewModel.Factory> {
        create(contractDetail.contractId)
      }
    ContractDetailDestination(
      viewModel = viewModel,
      onEditCoInsuredClick = dropUnlessResumed { contractId: String -> startEditCoInsured(contractId) },
      onEditCoOwnersClick = dropUnlessResumed { contractId: String -> startEditCoOwners(contractId) },
      onMissingCoInsuredInfoClick = dropUnlessResumed { contractId: String ->
        startEditCoInsuredAddMissingInfo(contractId)
      },
      onMissingCoOwnersInfoClick = dropUnlessResumed { contractId: String ->
        startEditCoOwnersAddMissingInfo(contractId)
      },
      onChangeAddressClick = dropUnlessResumed { startMovingFlow() },
      onCancelInsuranceClick = dropUnlessResumed { cancelInsuranceData: CancelInsuranceData ->
        startTerminationFlow(cancelInsuranceData)
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      openUrl = openUrl,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      imageLoader = imageLoader,
      onChangeTierClick = dropUnlessResumed { contractId: String ->
        onNavigateToStartChangeTier(contractId)
      },
      navigateToRemoveAddon = onNavigateToRemoveAddon,
      navigateToUpgradeAddon = navigateToUpgradeAddon,
      navigateToAddAddon = { availableAddon ->
        onNavigateToAddonPurchaseFlow(listOf(availableAddon.relatedContractId), availableAddon)
      },
      navigateToChipIdScreen = navigateToChipIdScreen,
    )
  }
  entry<TerminatedInsurancesKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: TerminatedContractsViewModel = metroViewModel()
    TerminatedContractsDestination(
      viewModel = viewModel,
      navigateToContractDetail = dropUnlessResumed { contractId: String ->
        backstack.add(InsuranceContractDetailKey(contractId))
      },
      navigateUp = backstack::navigateUp,
      imageLoader = imageLoader,
    )
  }
}
