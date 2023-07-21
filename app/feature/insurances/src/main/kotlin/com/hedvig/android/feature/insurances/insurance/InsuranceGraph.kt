package com.hedvig.android.feature.insurances

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  navigator: Navigator,
  openWebsite: (Uri) -> Unit,
  openChat: () -> Unit,
  startMovingFlow: () -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
) {
  animatedNavigation<TopLevelGraph.INSURANCE>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Insurance>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.insurances },
    ),
  ) {
    animatedComposable<AppDestination.TopLevelDestination.Insurance>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: InsuranceViewModel = koinViewModel()
      InsuranceDestination(
        viewModel = viewModel,
        onInsuranceCardClick = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestination.InsuranceContractDetail(contractId)) }
        },
        onCrossSellClick = { uri -> openWebsite(uri) },
        navigateToCancelledInsurances = {
          with(navigator) { backStackEntry.navigate(InsurancesDestination.TerminatedInsurances) }
        },
        openChat = openChat,
        imageLoader = imageLoader,
      )
    }
    animatedComposable<InsurancesDestination.InsuranceContractDetail> {
      val contractDetail = this
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
      ContractDetailDestination(
        viewModel = viewModel,
        onEditCoInsuredClick = openChat,
        onChangeAddressClick = { startMovingFlow() },
        onCancelInsuranceClick = { }, // open termination flow
        openWebsite = openWebsite,
        navigateUp = navigator::navigateUp,
        imageLoader = imageLoader,
      )
    }
    animatedComposable<InsurancesDestination.TerminatedInsurances> { backStackEntry ->
      val viewModel: TerminatedContractsViewModel = koinViewModel()
      TerminatedContractsDestination(
        viewModel = viewModel,
        navigateToContractDetail = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestination.InsuranceContractDetail(contractId)) }
        },
        navigateUp = navigator::navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}
