package com.hedvig.android.feature.payoutaccount.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountDestination
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountViewModel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewDestination
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.payoutAccountGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
) {
  navgraph<PayoutAccountDestination.Graph>(
    startDestination = PayoutAccountDestinations.Overview::class,
  ) {
    navdestination<PayoutAccountDestinations.Overview> {
      val viewModel: PayoutAccountOverviewViewModel = koinViewModel()
      PayoutAccountOverviewDestination(
        viewModel = viewModel,
        onEditBankAccountClicked = dropUnlessResumed {
          navController.navigate(PayoutAccountDestinations.EditBankAccount)
        },
        navigateUp = navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.EditBankAccount> {
      val viewModel: EditBankAccountViewModel = koinViewModel()
      EditBankAccountDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        navigateUp = navController::navigateUp,
      )
    }
  }
}
