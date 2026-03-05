package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.EditCoInsuredTriage
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navController: NavController, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
  navdestination<EditCoInsuredTriage>(
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.editCoInsured,
      hedvigDeepLinkContainer.editCoInsuredWithoutContractId,
    ),
  ) {
    val viewModel: EditCoInsuredTriageViewModel = koinViewModel { parametersOf(contractId) }
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

  navdestination<EditCoInsuredDestination.CoInsuredAddInfo> {
    EditCoInsuredAddMissingInfoDestination(
      viewModel = koinViewModel { parametersOf(contractId, type) },
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
    EditCoInsuredAddOrRemoveDestination(
      koinViewModel { parametersOf(contractId, type) },
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
