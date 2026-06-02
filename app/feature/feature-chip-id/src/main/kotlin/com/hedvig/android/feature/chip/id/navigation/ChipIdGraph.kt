package com.hedvig.android.feature.chip.id.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdDestination
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.chipIdGraph(
  backStack: MutableList<HedvigNavKey>,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  popBackStackOrFinish: () -> Unit,
  goHome: () -> Unit,
) {
  entry<AddChipIdTriageKey> { key ->
    val contractId = key.contractId
    LaunchedEffect(Unit) {
      backStack.navigateAndPopUpTo<AddChipIdTriageKey>(
        ChipIdKey(contractId = contractId),
        inclusive = true,
      )
    }
  }

  entry<ChipIdKey> { key ->
    val preselectedContractId = key.contractId
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
          backStack.navigateAndPopUpTo<ChipIdKey>(AddChipIdKey(contractId), inclusive = true)
        } else {
          backStack.add(AddChipIdKey(contractId))
        }
      },
    )
  }

  entry<AddChipIdKey> { key ->
    val contractId = key.contractId
    val viewModel: AddChipIdViewModel =
      assistedMetroViewModel<AddChipIdViewModel, AddChipIdViewModel.Factory> {
        create(contractId)
      }
    AddChipIdDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      navigateUp = {
        if (!backStack.navigateUp()) {
          goHome()
        }
      },
      popFlowOnSuccess = {
        if (backStack.findLastOrNull<ChipIdKey>() != null) {
          backStack.popUpTo<ChipIdKey>(inclusive = true)
        } else {
          goHome()
        }
      },
    )
  }
}
