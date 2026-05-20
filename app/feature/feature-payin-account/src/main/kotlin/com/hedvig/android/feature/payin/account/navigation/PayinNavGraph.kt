package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payin.account.ui.overview.PayoutAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayoutAccountOverviewUiState
import com.hedvig.android.feature.payin.account.ui.overview.PayoutAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayoutViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import octopus.type.MemberPaymentProvider
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.payinAccountGraph(
  navController: NavController,
  globalSnackBarState: GlobalSnackBarState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToConnectPayment: (builder: NavOptionsBuilder.() -> Unit) -> Unit,
  navigateUp: () -> Unit,
) {
  navgraph<PayinAccountDestination.Graph>(
    startDestination = PayinAccountDestinations.Overview::class,
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.payout),
  ) {
    navdestination<PayinAccountDestinations.Overview> {
      val viewModel: PayoutAccountOverviewViewModel = koinViewModel()
      PayoutAccountOverviewDestination(
        viewModel = viewModel,
        onConnectPayoutMethodClicked = dropUnlessResumed {
          val content = viewModel.uiState.value as? PayoutAccountOverviewUiState.Content
          navController.navigate(
            PayinAccountDestinations.SelectPayinMethod(
              availableProviders = content?.availablePayoutMethods?.map { it.rawValue } ?: emptyList(),
            ),
          )
        },
        navigateToConnectPayment = dropUnlessResumed {
          navigateToConnectPayment {
            typedPopUpTo<PayinAccountDestinations.Overview> {
              inclusive = true
            }
          }
        },
        navigateUp = navigateUp,
      )
    }

    navdestination<PayinAccountDestinations.SelectPayinMethod> {
      SelectPayoutMethodDestination(
        availableProviders = this.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
        onTrustlySelected = dropUnlessResumed {
          navigateToConnectPayment {
            typedPopUpTo<PayinAccountDestinations.SelectPayinMethod> {
              inclusive = true
            }
          }
        },
        onNordeaSelected = dropUnlessResumed { navController.navigate(PayinAccountDestinations.EditBankAccount) },
        onSwishSelected = dropUnlessResumed { navController.navigate(PayinAccountDestinations.SetupSwishPayin) },
        onInvoiceSelected = dropUnlessResumed { navController.navigate(PayinAccountDestinations.SetupInvoicePayin) },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayinAccountDestinations.SetupSwishPayin> {
      val viewModel: SetupSwishPayoutViewModel = koinViewModel()
      SetupSwishPayoutDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayinAccountDestinations.SelectPayinMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayinAccountDestinations.SetupInvoicePayin> {
      val viewModel: SetupInvoicePayoutViewModel = koinViewModel()
      SetupInvoicePayinDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayinAccountDestinations.SelectPayinMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
      )
    }
  }
}
