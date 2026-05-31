package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
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
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun NavGraphBuilder.editCoInsuredGraph(navController: NavController, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
  navdestination<EditCoInsuredTriage>(
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.editCoInsured,
      hedvigDeepLinkContainer.editCoInsuredWithoutContractId,
    ),
  ) {
    val triageContractId = contractId
    val triageType = type
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(triageContractId, triageType)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navController.navigate(EditCoInsuredDestination.CoInsuredAddInfo(contract.id, contract.type)) {
          typedPopUpTo<EditCoInsuredTriage> {
            inclusive = true
          }
        }
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navController.navigate(EditCoInsuredDestination.CoInsuredAddOrRemove(contract.id, contract.type)) {
          typedPopUpTo<EditCoInsuredTriage> {
            inclusive = true
          }
        }
      },
    )
  }

  navdestination<EditCoOwnersTriageDeepLink>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.editCoOwners),
  ) {
    val coOwnersContractId = contractId
    val viewModel: EditCoInsuredTriageViewModel =
      assistedMetroViewModel<EditCoInsuredTriageViewModel, EditCoInsuredTriageViewModel.Factory> {
        create(coOwnersContractId, CoInsuredFlowType.CoOwners)
      }
    EditCoInsuredTriageDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      navigateToAddMissingInfo = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navController.navigate(EditCoInsuredDestination.CoInsuredAddInfo(contract.id, contract.type)) {
          typedPopUpTo<EditCoOwnersTriageDeepLink> {
            inclusive = true
          }
        }
      },
      navigateToAddOrRemoveCoInsured = dropUnlessResumed { contract: InsuranceForEditOrAddCoInsured ->
        navController.navigate(EditCoInsuredDestination.CoInsuredAddOrRemove(contract.id, contract.type)) {
          typedPopUpTo<EditCoOwnersTriageDeepLink> {
            inclusive = true
          }
        }
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
        navController.navigate(EditCoInsuredDestination.Success(it, type)) {
          typedPopUpTo<EditCoInsuredDestination.CoInsuredAddInfo> {
            inclusive = true
          }
        }
      },
      navigateUp = navController::navigateUp,
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
        navController.navigate(EditCoInsuredDestination.Success(it, type)) {
          typedPopUpTo<EditCoInsuredDestination.CoInsuredAddOrRemove> {
            inclusive = true
          }
        }
      },
      navigateUp = navController::navigateUp,
    )
  }
  navdestination<EditCoInsuredDestination.Success>(
    EditCoInsuredDestination.Success,
  ) {
    EditCoInsuredSuccessDestination(
      date = date,
      type = type,
      navigateUp = navController::navigateUp,
      navigateBack = navController::popBackStack,
    )
  }
}
