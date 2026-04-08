package com.hedvig.android.feature.chip.id.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdDestination
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.chipIdGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  popBackStack: () -> Unit
) {
  navdestination<ChipIdDestination.AddChipIdTriage>(
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.petIdWithoutContractId,
      hedvigDeepLinkContainer.petIdWithContractId,
    ),
  ) {
    val contractId = this.contractId
    LaunchedEffect(Unit) {
      navController.navigate(
        ChipIdGraphDestination(
          contractId = contractId,
        ),
      ) {
        typedPopUpTo<ChipIdDestination.AddChipIdTriage>({ inclusive = true })
      }
    }
  }

  navgraph<ChipIdGraphDestination>(
    startDestination = ChipIdDestination.SelectInsuranceForChipId::class,
    destinationNavTypeAware = ChipIdGraphDestination,
  ) {
    navdestination<ChipIdDestination.SelectInsuranceForChipId> { backStackEntry ->
      val chipIdGraphDestination = navController
        .getRouteFromBackStack<ChipIdGraphDestination>(backStackEntry)
      val preselectedContractId = chipIdGraphDestination.contractId

      val viewModel: SelectInsuranceForChipIdViewModel = koinViewModel {
        parametersOf(preselectedContractId)
      }
      SelectInsuranceForChipIdDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
        popBackStack = popBackStack,
        navigateToAddChipId = { contractId: String, popSelectInsurance: Boolean ->
          navController.navigate(ChipIdDestination.AddChipId(contractId)) {
            if (popSelectInsurance) {
              typedPopUpTo<ChipIdGraphDestination> {
                inclusive = true
              }
            }
          }
        },
      )
    }

    navdestination<ChipIdDestination.AddChipId> { backStackEntry ->
      val contractId = this.contractId
      val viewModel: AddChipIdViewModel = koinViewModel {
        parametersOf(contractId)
      }
      AddChipIdDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        navigateUp = navigateUp,
        popFlowOnSuccess = {
          navController.popBackStack(ChipIdGraphDestination::class, inclusive = true)
        },
      )
    }
  }
}


