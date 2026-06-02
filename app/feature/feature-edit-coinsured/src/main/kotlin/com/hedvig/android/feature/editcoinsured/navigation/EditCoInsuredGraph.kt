package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.editCoInsuredGraph(backStack: MutableList<HedvigNavKey>) {
  entry<EditCoInsuredTriageKey> { key ->
    val triageContractId = key.contractId
    val triageType = key.type
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(triageContractId, triageType)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        backStack.navigateAndPopUpTo<EditCoInsuredTriageKey>(
          CoInsuredAddInfoKey(contract.id, contract.type),
          inclusive = true,
        )
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        backStack.navigateAndPopUpTo<EditCoInsuredTriageKey>(
          CoInsuredAddOrRemoveKey(contract.id, contract.type),
          inclusive = true,
        )
      },
    )
  }

  entry<EditCoOwnersTriageDeepLinkKey> { key ->
    val coOwnersContractId = key.contractId
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(coOwnersContractId, CoInsuredFlowType.CoOwners)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        backStack.navigateAndPopUpTo<EditCoOwnersTriageDeepLinkKey>(
          CoInsuredAddInfoKey(contract.id, contract.type),
          inclusive = true,
        )
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        backStack.navigateAndPopUpTo<EditCoOwnersTriageDeepLinkKey>(
          CoInsuredAddOrRemoveKey(contract.id, contract.type),
          inclusive = true,
        )
      },
    )
  }

  entry<CoInsuredAddInfoKey> { key ->
    val addInfoContractId = key.contractId
    val addInfoType = key.type
    EditCoInsuredAddMissingInfoDestination(
      viewModel = assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addInfoContractId, addInfoType)
      },
      navigateToSuccessScreen = {
        backStack.navigateAndPopUpTo<CoInsuredAddInfoKey>(
          EditCoInsuredSuccessKey(it, key.type),
          inclusive = true,
        )
      },
      navigateUp = backStack::navigateUp,
    )
  }
  entry<CoInsuredAddOrRemoveKey> { key ->
    val addOrRemoveContractId = key.contractId
    val addOrRemoveType = key.type
    EditCoInsuredAddOrRemoveDestination(
      assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addOrRemoveContractId, addOrRemoveType)
      },
      navigateToSuccessScreen = {
        backStack.navigateAndPopUpTo<CoInsuredAddOrRemoveKey>(
          EditCoInsuredSuccessKey(it, key.type),
          inclusive = true,
        )
      },
      navigateUp = backStack::navigateUp,
    )
  }
  entry<EditCoInsuredSuccessKey> { key ->
    EditCoInsuredSuccessDestination(
      date = key.date,
      type = key.type,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
    )
  }
}
