package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigator: Navigator) {
  navgraph<AppDestination.EditCoInsured>(
    startDestination = AppDestination.CoInsuredAddOrRemove::class,
  ) {
    navdestination<AppDestination.CoInsuredAddInfo> {
      EditCoInsuredAddMissingInfoDestination(
        viewModel = koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navigator.navigateUnsafe(EditCoInsuredDestination.Success(it)) {
            typedPopUpTo<AppDestination.EditCoInsured> {
              inclusive = true
            }
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<AppDestination.CoInsuredAddOrRemove> {
      EditCoInsuredAddOrRemoveDestination(
        koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navigator.navigateUnsafe(EditCoInsuredDestination.Success(it)) {
            typedPopUpTo<AppDestination.EditCoInsured> {
              inclusive = true
            }
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<EditCoInsuredDestination.Success>(
      EditCoInsuredDestination.Success,
    ) {
      EditCoInsuredSuccessDestination(
        date = date,
        popBackstack = navigator::popBackStack,
      )
    }
  }
}
