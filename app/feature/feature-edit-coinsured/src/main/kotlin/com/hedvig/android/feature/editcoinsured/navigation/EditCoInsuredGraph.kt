package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.EditCoInsuredTriage
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.EditCoOwnersTriageDeepLink
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredViewModel
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<Destination>.editCoInsuredGraph(navigator: Navigator) {
  navdestination<EditCoInsuredTriage> {
    val triageContractId = contractId
    val triageType = type
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(triageContractId, triageType)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navigator.navigate<EditCoInsuredTriage>(
          EditCoInsuredDestination.CoInsuredAddInfo(contract.id, contract.type),
          inclusive = true,
        )
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navigator.navigate<EditCoInsuredTriage>(
          EditCoInsuredDestination.CoInsuredAddOrRemove(contract.id, contract.type),
          inclusive = true,
        )
      },
    )
  }

  navdestination<EditCoOwnersTriageDeepLink> {
    val coOwnersContractId = contractId
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(coOwnersContractId, CoInsuredFlowType.CoOwners)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navigator.navigate<EditCoOwnersTriageDeepLink>(
          EditCoInsuredDestination.CoInsuredAddInfo(contract.id, contract.type),
          inclusive = true,
        )
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navigator.navigate<EditCoOwnersTriageDeepLink>(
          EditCoInsuredDestination.CoInsuredAddOrRemove(contract.id, contract.type),
          inclusive = true,
        )
      },
    )
  }

  navdestination<EditCoInsuredDestination.CoInsuredAddInfo> {
    val addInfoContractId = contractId
    val addInfoType = type
    EditCoInsuredAddMissingInfoDestination(
      viewModel = assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addInfoContractId, addInfoType)
      },
      navigateToSuccessScreen = {
        navigator.navigate<EditCoInsuredDestination.CoInsuredAddInfo>(
          EditCoInsuredDestination.Success(it, type),
          inclusive = true,
        )
      },
      navigateUp = navigator::navigateUp,
    )
  }
  navdestination<EditCoInsuredDestination.CoInsuredAddOrRemove> {
    val addOrRemoveContractId = contractId
    val addOrRemoveType = type
    EditCoInsuredAddOrRemoveDestination(
      assistedMetroViewModel<EditCoInsuredViewModel, EditCoInsuredViewModel.Factory> {
        create(addOrRemoveContractId, addOrRemoveType)
      },
      navigateToSuccessScreen = {
        navigator.navigate<EditCoInsuredDestination.CoInsuredAddOrRemove>(
          EditCoInsuredDestination.Success(it, type),
          inclusive = true,
        )
      },
      navigateUp = navigator::navigateUp,
    )
  }
  navdestination<EditCoInsuredDestination.Success> {
    EditCoInsuredSuccessDestination(
      date = date,
      type = type,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
}
