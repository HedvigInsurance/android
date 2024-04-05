package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigateUp: () -> Unit, navController: NavHostController) {
  navigation<AppDestination.EditCoInsured>(
    startDestination = AppDestination.CoInsuredAddOrRemove::class,
  ) {
    composable<AppDestination.CoInsuredAddInfo> { _, destination ->
      EditCoInsuredAddMissingInfoDestination(
        viewModel = koinViewModel { parametersOf(destination.contractId) },
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
    composable<AppDestination.CoInsuredAddOrRemove> { _, destination ->
      EditCoInsuredAddOrRemoveDestination(
        koinViewModel { parametersOf(destination.contractId) },
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
    composable<EditCoInsuredDestination.Success> { _, destination ->
      EditCoInsuredSuccessDestination(
        date = destination.date,
        popBackstack = {
          navController.popBackStack()
        },
      )
    }
  }
}
