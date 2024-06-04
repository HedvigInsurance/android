package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.navigation.core.AppDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigateUp: () -> Unit, navController: NavHostController) {
  navigation<AppDestination.EditCoInsured>(
    startDestination = createRoutePattern<AppDestination.CoInsuredAddOrRemove>(),
  ) {
    composable<AppDestination.CoInsuredAddInfo> {
      EditCoInsuredAddMissingInfoDestination(
        viewModel = koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navController.navigate(EditCoInsuredDestination.Success(it)) {
            popUpTo<AppDestination.EditCoInsured> {
              inclusive = true
            }
          }
        },
        navigateUp = navigateUp,
      )
    }
    composable<AppDestination.CoInsuredAddOrRemove> {
      EditCoInsuredAddOrRemoveDestination(
        koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navController.navigate(EditCoInsuredDestination.Success(it)) {
            popUpTo<AppDestination.EditCoInsured> {
              inclusive = true
            }
          }
        },
        navigateUp = navigateUp,
      )
    }
    composable<EditCoInsuredDestination.Success> {
      EditCoInsuredSuccessDestination(
        date = date,
        popBackstack = {
          navController.popBackStack()
        },
      )
    }
  }
}
