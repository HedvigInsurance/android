package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.EditCoInsuredTriage
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageDestination
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigator: Navigator, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
  navgraph<EditCoInsuredGraphDestination>(
    startDestination = EditCoInsuredDestination.EditCoInsuredTriage::class,
  ) {
    navdestination<EditCoInsuredTriage>(
      deepLinks = navDeepLinks(
        hedvigDeepLinkContainer.editCoInsured,
        hedvigDeepLinkContainer.editCoInsuredWithoutContractId,
      ),
    ) {
      val viewModel: EditCoInsuredTriageViewModel = koinViewModel { parametersOf(contractId) }
      EditCoInsuredTriageDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToAddMissingInfo = { contractId: String ->
          navigator.navigateUnsafe(EditCoInsuredDestination.CoInsuredAddInfo(contractId)) {
            typedPopUpTo<EditCoInsuredGraphDestination> {
              inclusive = true
            }
          }
        },
        navigateToAddOrRemoveCoInsured = { contractId: String ->
          navigator.navigateUnsafe(EditCoInsuredDestination.CoInsuredAddOrRemove(contractId)) {
            typedPopUpTo<EditCoInsuredGraphDestination> {
              inclusive = true
            }
          }
        },
      )
    }
    navdestination<EditCoInsuredDestination.CoInsuredAddInfo> {
      EditCoInsuredAddMissingInfoDestination(
        viewModel = koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navigator.navigateUnsafe(EditCoInsuredDestination.Success(it)) {
            typedPopUpTo<EditCoInsuredGraphDestination> {
              inclusive = true
            }
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<EditCoInsuredDestination.CoInsuredAddOrRemove> {
      EditCoInsuredAddOrRemoveDestination(
        koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navigator.navigateUnsafe(EditCoInsuredDestination.Success(it)) {
            typedPopUpTo<EditCoInsuredGraphDestination> {
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
