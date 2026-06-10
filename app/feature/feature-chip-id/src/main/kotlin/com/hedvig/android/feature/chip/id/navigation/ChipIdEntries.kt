package com.hedvig.android.feature.chip.id.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdDestination
import com.hedvig.android.feature.chip.id.ui.selectinsurance.SelectInsuranceForChipIdViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.chipIdEntries(
  backstack: Backstack,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
  goHome: () -> Unit,
) {
  entry<AddChipIdTriageKey> { key ->
    val contractId = key.contractId
    LaunchedEffect(Unit) {
      backstack.navigateAndPopUpTo<AddChipIdTriageKey>(
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
      popBackstack = backstack::popBackstack,
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
        if (!backstack.navigateUp()) {
          goHome()
        }
      },
      popFlowOnSuccess = {
        if (backstack.findLastOrNull<ChipIdKey>() != null) {
          backstack.popUpTo<ChipIdKey>(inclusive = true)
        } else {
          goHome()
        }
      },
    )
  }
}
