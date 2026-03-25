package com.hedvig.android.feature.chip.id.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.chipIdGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
) {
  navgraph<ChipIdGraphDestination>(
    startDestination = ChipIdDestination.AddChipId::class,
    destinationNavTypeAware = ChipIdGraphDestination,
  ) {
    navdestination<ChipIdDestination.AddChipId> { backStackEntry ->
      val chipIdGraphDestination = navController
        .getRouteFromBackStack<ChipIdGraphDestination>(backStackEntry)
      val viewModel: AddChipIdViewModel = koinViewModel {
        parametersOf(chipIdGraphDestination.contractId)
      }
      AddChipIdDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        navigateUp = navigateUp,
        popBackStack = {
          navController.popBackStack()
        },
      )
    }
  }
}
