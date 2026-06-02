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
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.editCoInsuredGraph(backStack: MutableList<HedvigNavKey>) {
  navdestination<EditCoInsuredTriageKey> {
    val triageContractId = contractId
    val triageType = type
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

  navdestination<EditCoOwnersTriageDeepLinkKey> {
    val coOwnersContractId = contractId
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

  navdestination<CoInsuredAddInfoKey> {
    val addInfoContractId = contractId
    val addInfoType = type
    EditCoInsuredAddMissingInfoDestination(
      viewModel = assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addInfoContractId, addInfoType)
      },
      navigateToSuccessScreen = {
        backStack.navigateAndPopUpTo<CoInsuredAddInfoKey>(
          EditCoInsuredSuccessKey(it, type),
          inclusive = true,
        )
      },
      navigateUp = backStack::navigateUp,
    )
  }
  navdestination<CoInsuredAddOrRemoveKey> {
    val addOrRemoveContractId = contractId
    val addOrRemoveType = type
    EditCoInsuredAddOrRemoveDestination(
      assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addOrRemoveContractId, addOrRemoveType)
      },
      navigateToSuccessScreen = {
        backStack.navigateAndPopUpTo<CoInsuredAddOrRemoveKey>(
          EditCoInsuredSuccessKey(it, type),
          inclusive = true,
        )
      },
      navigateUp = backStack::navigateUp,
    )
  }
  navdestination<EditCoInsuredSuccessKey> {
    EditCoInsuredSuccessDestination(
      date = date,
      type = type,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
    )
  }
}
