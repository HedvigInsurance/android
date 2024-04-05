package com.hedvig.android.feature.insurances.insurance

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestinations
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  startMovingFlow: (NavBackStackEntry) -> Unit,
  startTerminationFlow: (backStackEntry: NavBackStackEntry, cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
) {
  navigation<InsurancesDestination.Graph>(
    startDestination = InsurancesDestination.Insurances::class,
  ) {
    nestedGraphs()
    composable<InsurancesDestination.Insurances>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.insurances },
        navDeepLink { uriPattern = hedvigDeepLinkContainer.contractWithoutContractId },
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
        imageLoader = imageLoader,
      )
    }
    composable<InsurancesDestinations.InsuranceContractDetail>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.contract },
      ),
    ) { backStackEntry, destination ->
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(destination.contractId) }
      ContractDetailDestination(
        viewModel = viewModel,
        onEditCoInsuredClick = { contractId: String -> startEditCoInsured(backStackEntry, contractId) },
        onMissingInfoClick = { contractId -> startEditCoInsuredAddMissingInfo(backStackEntry, contractId) },
        onChangeAddressClick = { startMovingFlow(backStackEntry) },
        onCancelInsuranceClick = { cancelInsuranceData: CancelInsuranceData ->
          startTerminationFlow(
            backStackEntry,
            cancelInsuranceData,
          )
        },
        openChat = { openChat(backStackEntry) },
        openUrl = openUrl,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
        imageLoader = imageLoader,
      )
    }
    composable<InsurancesDestinations.TerminatedInsurances> { backStackEntry ->
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
