package com.hedvig.android.feature.insurances.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
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
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  startMovingFlow: () -> Unit,
  onNavigateToStartChangeTier: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  startTerminationFlow: (backStackEntry: NavBackStackEntry, cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
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
    ) { backStackEntry ->
      val viewModel: InsuranceViewModel = koinViewModel()
      InsuranceDestination(
        viewModel = viewModel,
        onInsuranceCardClick = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.InsuranceContractDetail(contractId)) }
        },
        onCrossSellClick = openUrl,
        navigateToCancelledInsurances = {
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.TerminatedInsurances) }
        },
        onNavigateToMovingFlow = startMovingFlow,
        imageLoader = imageLoader,
        onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
      )
    }
    navdestination<InsurancesDestinations.InsuranceContractDetail>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.contract),
    ) { backStackEntry ->
      val contractDetail = this
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
      ContractDetailDestination(
        viewModel = viewModel,
        onEditCoInsuredClick = { contractId: String -> startEditCoInsured(backStackEntry, contractId) },
        onMissingInfoClick = { contractId -> startEditCoInsuredAddMissingInfo(backStackEntry, contractId) },
        onChangeAddressClick = startMovingFlow,
        onCancelInsuranceClick = { cancelInsuranceData: CancelInsuranceData ->
          startTerminationFlow(
            backStackEntry,
            cancelInsuranceData,
          )
        },
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
        openUrl = openUrl,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
        imageLoader = imageLoader,
        onChangeTierClick = { contractId: String ->
          onNavigateToStartChangeTier(backStackEntry, contractId)
        },
      )
    }
    navdestination<InsurancesDestinations.TerminatedInsurances> { backStackEntry ->
      val viewModel: TerminatedContractsViewModel = koinViewModel()
      TerminatedContractsDestination(
        viewModel = viewModel,
        navigateToContractDetail = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.InsuranceContractDetail(contractId)) }
        },
        navigateUp = navigator::navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}
