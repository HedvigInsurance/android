package com.hedvig.android.feature.chip.id.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdDestination
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.chipIdGraph(
  navigator: Navigator,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  popBackStackOrFinish: () -> Unit,
  goHome: () -> Unit,
) {
  navdestination<ChipIdDestination.AddChipIdTriage> {
    val contractId = this.contractId
    LaunchedEffect(Unit) {
      navigator.navigate<ChipIdDestination.AddChipIdTriage>(
        ChipIdGraphDestination(contractId = contractId),
        inclusive = true,
      )
    }
  }

  navdestination<ChipIdGraphDestination> {
    val preselectedContractId = this.contractId
    val viewModel: SelectInsuranceForChipIdViewModel =
      assistedMetroViewModel<SelectInsuranceForChipIdViewModel, SelectInsuranceForChipIdViewModel.Factory> {
        create(preselectedContractId)
      }
    SelectInsuranceForChipIdDestination(
      viewModel = viewModel,
      navigateUp = navigateUp,
      popBackStack = popBackStackOrFinish,
      navigateToAddChipId = { contractId: String, popSelectInsurance: Boolean ->
        if (popSelectInsurance) {
          navigator.navigate<ChipIdGraphDestination>(ChipIdDestination.AddChipId(contractId), inclusive = true)
        } else {
          navigator.navigate(ChipIdDestination.AddChipId(contractId))
        }
      },
    )
  }

  navdestination<ChipIdDestination.AddChipId> {
    val contractId = this.contractId
    val viewModel: AddChipIdViewModel =
      assistedMetroViewModel<AddChipIdViewModel, AddChipIdViewModel.Factory> {
        create(contractId)
      }
    AddChipIdDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      navigateUp = {
        if (!navigator.navigateUp()) {
          goHome()
        }
      },
      popFlowOnSuccess = {
        if (navigator.findLastOrNull<ChipIdGraphDestination>() != null) {
          navigator.popUpTo<ChipIdGraphDestination>(inclusive = true)
        } else {
          goHome()
        }
      },
    )
  }
}
