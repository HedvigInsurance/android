package com.hedvig.android.feature.insurances.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.insurances.data.AvailableAddon
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.InsuranceDestination
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navController: NavController,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  startMovingFlow: () -> Unit,
  onNavigateToStartChangeTier: (contractId: String) -> Unit,
  startTerminationFlow: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (contractId: String) -> Unit,
  startEditCoOwners: (contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (contractId: String) -> Unit,
  startEditCoOwnersAddMissingInfo: (contractId: String) -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<ContractId>, AvailableAddon?) -> Unit,
  onNavigateToRemoveAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToUpgradeAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToChipIdScreen: () -> Unit,
) {
  navgraph<InsurancesDestination.Graph>(
    startDestination = InsurancesDestination.Insurances::class,
  ) {
    nestedGraphs()
    navdestination<InsurancesDestination.Insurances>(
      deepLinks = navDeepLinks(
        hedvigDeepLinkContainer.insurances,
        hedvigDeepLinkContainer.contractWithoutContractId,
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: InsuranceViewModel = koinViewModel()
      InsuranceDestination(
        viewModel = viewModel,
        onInsuranceCardClick = dropUnlessResumed { contractId: String ->
          navController.navigate(InsurancesDestinations.InsuranceContractDetail(contractId))
        },
        onCrossSellClick = dropUnlessResumed { url: String -> openUrl(url) },
        navigateToCancelledInsurances = dropUnlessResumed {
          navController.navigate(InsurancesDestinations.TerminatedInsurances)
        },
        onNavigateToMovingFlow = dropUnlessResumed { startMovingFlow() },
        imageLoader = imageLoader,
        onNavigateToAddonPurchaseFlow = dropUnlessResumed { ids: List<ContractId> ->
          onNavigateToAddonPurchaseFlow(ids, null)
        },
      )
    }
    navdestination<InsurancesDestinations.InsuranceContractDetail>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.contract),
    ) {
      val contractDetail = this
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
      ContractDetailDestination(
        viewModel = viewModel,
        onEditCoInsuredClick = dropUnlessResumed { contractId: String -> startEditCoInsured(contractId) },
        onEditCoOwnersClick = dropUnlessResumed { contractId: String -> startEditCoOwners(contractId) },
        onMissingCoInsuredInfoClick = dropUnlessResumed { contractId: String -> startEditCoInsuredAddMissingInfo(contractId) },
        onMissingCoOwnersInfoClick = dropUnlessResumed { contractId: String -> startEditCoOwnersAddMissingInfo(contractId) },
        onChangeAddressClick = dropUnlessResumed { startMovingFlow() },
        onCancelInsuranceClick = dropUnlessResumed { cancelInsuranceData: CancelInsuranceData ->
          startTerminationFlow(cancelInsuranceData)
        },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        openUrl = openUrl,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        imageLoader = imageLoader,
        onChangeTierClick = dropUnlessResumed { contractId: String ->
          onNavigateToStartChangeTier(contractId)
        },
        navigateToRemoveAddon = onNavigateToRemoveAddon,
        navigateToUpgradeAddon = navigateToUpgradeAddon,
        navigateToAddAddon = { availableAddon ->
          onNavigateToAddonPurchaseFlow(listOf(availableAddon.relatedContractId), availableAddon)
        },
        navigateToChipIdScreen = navigateToChipIdScreen
      )
    }
    navdestination<InsurancesDestinations.TerminatedInsurances> {
      val viewModel: TerminatedContractsViewModel = koinViewModel()
      TerminatedContractsDestination(
        viewModel = viewModel,
        navigateToContractDetail = dropUnlessResumed { contractId: String ->
          navController.navigate(InsurancesDestinations.InsuranceContractDetail(contractId))
        },
        navigateUp = navController::navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}
