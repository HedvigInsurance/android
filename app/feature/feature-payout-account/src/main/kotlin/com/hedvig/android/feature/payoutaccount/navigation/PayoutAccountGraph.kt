package com.hedvig.android.feature.payoutaccount.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountDestination
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountViewModel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewDestination
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewViewModel
import com.hedvig.android.feature.payoutaccount.ui.selectmethod.SelectPayoutMethodDestination
import com.hedvig.android.feature.payoutaccount.ui.setupinvoice.SetupInvoicePayoutDestination
import com.hedvig.android.feature.payoutaccount.ui.setupinvoice.SetupInvoicePayoutViewModel
import com.hedvig.android.feature.payoutaccount.ui.setupswish.SetupSwishPayoutDestination
import com.hedvig.android.feature.payoutaccount.ui.setupswish.SetupSwishPayoutViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import octopus.type.MemberPaymentProvider
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.payoutAccountGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToTrustlyPayout: (builder: NavOptionsBuilder.() -> Unit) -> Unit,
  navigateBack: () -> Unit,
  navigateUp: () -> Unit,
) {
  navgraph<PayoutAccountDestination.Graph>(
    startDestination = PayoutAccountDestinations.Overview::class,
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.payout)
  ) {
    navdestination<PayoutAccountDestinations.Overview> {
      val viewModel: PayoutAccountOverviewViewModel = koinViewModel()
      PayoutAccountOverviewDestination(
        viewModel = viewModel,
        onConnectPayoutMethodClicked = dropUnlessResumed {
          val content = viewModel.uiState.value as? PayoutAccountOverviewUiState.Content
          navController.navigate(
            PayoutAccountDestinations.SelectPayoutMethod(
              availableProviders = content?.availablePayoutMethods?.map { it.rawValue } ?: emptyList(),
            ),
          )
        },
        navigateBack = navigateBack,
        navigateUp = navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SelectPayoutMethod> {
      SelectPayoutMethodDestination(
        availableProviders = this.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
        onTrustlySelected = dropUnlessResumed {
          navigateToTrustlyPayout {
            typedPopUpTo<PayoutAccountDestinations.SelectPayoutMethod> {
              inclusive = true
            }
          }
        },
        onNordeaSelected = dropUnlessResumed { navController.navigate(PayoutAccountDestinations.EditBankAccount) },
        onSwishSelected = dropUnlessResumed { navController.navigate(PayoutAccountDestinations.SetupSwishPayout) },
        onInvoiceSelected = dropUnlessResumed { navController.navigate(PayoutAccountDestinations.SetupInvoicePayout) },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.EditBankAccount> {
      val viewModel: EditBankAccountViewModel = koinViewModel()
      EditBankAccountDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SetupSwishPayout> {
      val viewModel: SetupSwishPayoutViewModel = koinViewModel()
      SetupSwishPayoutDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SetupInvoicePayout> {
      val viewModel: SetupInvoicePayoutViewModel = koinViewModel()
      SetupInvoicePayoutDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
      )
    }
  }
}
