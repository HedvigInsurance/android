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
import dev.zacsweers.metrox.viewmodel.metroViewModel
import octopus.type.MemberPaymentProvider

fun NavGraphBuilder.payoutAccountGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToConnectPayment: (builder: NavOptionsBuilder.() -> Unit) -> Unit,
  navigateUp: () -> Unit,
) {
  navgraph<PayoutAccountDestination.Graph>(
    startDestination = PayoutAccountDestinations.Overview::class,
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.payout),
  ) {
    navdestination<PayoutAccountDestinations.Overview> {
      val viewModel: PayoutAccountOverviewViewModel = metroViewModel()
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
        navigateToConnectPayment = dropUnlessResumed {
          navigateToConnectPayment {
            typedPopUpTo<PayoutAccountDestinations.Overview> {
              inclusive = true
            }
          }
        },
        navigateUp = navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SelectPayoutMethod> {
      SelectPayoutMethodDestination(
        availableProviders = this.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
        onTrustlySelected = dropUnlessResumed {
          navigateToConnectPayment {
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
      val viewModel: EditBankAccountViewModel = metroViewModel()
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
      val viewModel: SetupSwishPayoutViewModel = metroViewModel()
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
      val viewModel: SetupInvoicePayoutViewModel = metroViewModel()
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
