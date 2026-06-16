package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModelFactory
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModelFactory
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.editCoInsuredEntries(backstack: Backstack) {
  entry<EditCoInsuredTriageKey> { key ->
    val triageContractId = key.contractId
    val triageType = key.type
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModelFactory> {
        create(triageContractId, triageType)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<EditCoOwnersTriageDeepLinkKey> { key ->
    val coOwnersContractId = key.contractId
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModelFactory> {
        create(coOwnersContractId, CoInsuredFlowType.CoOwners)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<CoInsuredAddInfoKey> { key ->
    val addInfoContractId = key.contractId
    val addInfoType = key.type
    EditCoInsuredAddMissingInfoDestination(
      viewModel = assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModelFactory> {
        create(addInfoContractId, addInfoType)
      },
      navigateUp = backstack::navigateUp,
    )
  }
  entry<CoInsuredAddOrRemoveKey> { key ->
    val addOrRemoveContractId = key.contractId
    val addOrRemoveType = key.type
    EditCoInsuredAddOrRemoveDestination(
      assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModelFactory> {
        create(addOrRemoveContractId, addOrRemoveType)
      },
      navigateUp = backstack::navigateUp,
    )
  }
  entry<EditCoInsuredSuccessKey> { key ->
    EditCoInsuredSuccessDestination(
      date = key.date,
      type = key.type,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }
}
