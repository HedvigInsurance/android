package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewUiState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinViewModel
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
  navigateToConnectTrustly: (builder: NavOptionsBuilder.() -> Unit) -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<PayinAccountDestination.Graph>(
    startDestination = PayinAccountDestinations.Overview::class,
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.connectPayment),
  ) {
    navdestination<PayinAccountDestinations.Overview> {
      val viewModel: PayinAccountOverviewViewModel = koinViewModel()
      PayinAccountOverviewDestination(
        viewModel = viewModel,
        onConnectPayoutMethodClicked = dropUnlessResumed {
          val content = viewModel.uiState.value as? PayinAccountOverviewUiState.Content
          navController.navigate(
            PayinAccountDestinations.SelectPayinMethod(
              availableProviders = content?.availablePayinMethods?.map { it.rawValue } ?: emptyList(),
            ),
          )
        },
        navigateUp = navigateUp,
      )
    }

    navdestination<PayinAccountDestinations.SelectPayinMethod> {
      SelectPayinMethodDestination(
        availableProviders = this.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
        onTrustlySelected = dropUnlessResumed {
          navigateToConnectTrustly {
            typedPopUpTo<PayinAccountDestinations.SelectPayinMethod> {
              inclusive = true
            }
          }
        },
        onSwishSelected = dropUnlessResumed { navController.navigate(PayinAccountDestinations.SetupSwishPayin) },
        onInvoiceSelected = dropUnlessResumed { navController.navigate(PayinAccountDestinations.SetupInvoicePayin) },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PayinAccountDestinations.SetupSwishPayin> {
      val viewModel: SetupSwishPayinViewModel = koinViewModel()
      SetupSwishPayinDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navController.typedPopBackStack<PayinAccountDestinations.SelectPayinMethod>(inclusive = true)
        },
        navigateUp = navController::navigateUp,
        openUrl = {
          navController.typedPopBackStack<PayinAccountDestinations.SelectPayinMethod>(inclusive = true)
          openUrl(it)
        }
      )
    }

    navdestination<PayinAccountDestinations.SetupInvoicePayin> {
      val viewModel: SetupInvoicePayinViewModel = koinViewModel()
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
