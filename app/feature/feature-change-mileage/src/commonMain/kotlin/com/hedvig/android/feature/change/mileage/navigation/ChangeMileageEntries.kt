package com.hedvig.android.feature.change.mileage.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.change.mileage.ui.choose.ChooseInsuranceForMileageDestination
import com.hedvig.android.feature.change.mileage.ui.choose.ChooseInsuranceForMileageViewModel
import com.hedvig.android.feature.change.mileage.ui.result.ChangeMileageFailureScreen
import com.hedvig.android.feature.change.mileage.ui.result.ChangeMileageSuccessScreen
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileageDestination
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileageViewModel
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileageViewModelFactory
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryDestination
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryViewModel
import com.hedvig.android.feature.change.mileage.ui.summary.ChangeMileageSummaryViewModelFactory
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.changeMileageEntries(backstack: Backstack) {
  entry<ChangeMileageChooseInsuranceKey> {
    val viewModel: ChooseInsuranceForMileageViewModel = metroViewModel()
    ChooseInsuranceForMileageDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
    )
  }

  entry<ChangeMileageKey> { key ->
    val contractId = key.contractId
    val viewModel: ChangeMileageViewModel =
      assistedMetroViewModel<ChangeMileageViewModel, ChangeMileageViewModelFactory> {
        create(contractId)
      }
    ChangeMileageDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
    )
  }

  entry<ChangeMileageSummaryKey> { key ->
    val params = key.params
    val viewModel: ChangeMileageSummaryViewModel =
      assistedMetroViewModel<ChangeMileageSummaryViewModel, ChangeMileageSummaryViewModelFactory> {
        create(params)
      }
    ChangeMileageSummaryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onExitFlow = { backstack.popUpTo<ChangeMileageKey>(inclusive = true) },
    )
  }

  entry<ChangeMileageSuccessKey> { key ->
    ChangeMileageSuccessScreen(
      newMileageDisplayName = key.newMileageDisplayName,
      newMonthlyCost = key.newMonthlyCost,
      popBackstack = backstack::popBackstack,
    )
  }

  entry<ChangeMileageFailureKey> {
    ChangeMileageFailureScreen(popBackstack = backstack::popBackstack)
  }
}
