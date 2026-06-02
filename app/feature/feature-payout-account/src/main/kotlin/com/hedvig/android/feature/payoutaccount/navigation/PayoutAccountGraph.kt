package com.hedvig.android.feature.payoutaccount.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.metroViewModel
import octopus.type.MemberPaymentProvider

fun EntryProviderScope<HedvigNavKey>.payoutAccountGraph(
  navigator: Navigator,
  globalSnackBarState: GlobalSnackBarState,
  navigateToConnectPayment: () -> Unit,
  navigateUp: () -> Unit,
) {
  navgraph(
    startDestination = PayoutAccountDestination.Graph::class,
  ) {
    navdestination<PayoutAccountDestination.Graph> {
      val viewModel: PayoutAccountOverviewViewModel = metroViewModel()
      PayoutAccountOverviewDestination(
        viewModel = viewModel,
        onConnectPayoutMethodClicked = dropUnlessResumed {
          val content = viewModel.uiState.value as? PayoutAccountOverviewUiState.Content
          navigator.navigate(
            PayoutAccountDestinations.SelectPayoutMethod(
              availableProviders = content?.availablePayoutMethods?.map { it.rawValue } ?: emptyList(),
            ),
          )
        },
        navigateToConnectPayment = dropUnlessResumed {
          navigator.popUpTo<PayoutAccountDestination.Graph>(inclusive = true)
          navigateToConnectPayment()
        },
        navigateUp = navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SelectPayoutMethod> {
      SelectPayoutMethodDestination(
        availableProviders = this.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
        onTrustlySelected = dropUnlessResumed {
          navigator.popUpTo<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
          navigateToConnectPayment()
        },
        onNordeaSelected = dropUnlessResumed { navigator.navigate(PayoutAccountDestinations.EditBankAccount) },
        onSwishSelected = dropUnlessResumed { navigator.navigate(PayoutAccountDestinations.SetupSwishPayout) },
        onInvoiceSelected = dropUnlessResumed { navigator.navigate(PayoutAccountDestinations.SetupInvoicePayout) },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.EditBankAccount> {
      val viewModel: EditBankAccountViewModel = metroViewModel()
      EditBankAccountDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navigator.popUpTo<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SetupSwishPayout> {
      val viewModel: SetupSwishPayoutViewModel = metroViewModel()
      SetupSwishPayoutDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navigator.popUpTo<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PayoutAccountDestinations.SetupInvoicePayout> {
      val viewModel: SetupInvoicePayoutViewModel = metroViewModel()
      SetupInvoicePayoutDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        onSuccessfullyConnected = {
          navigator.popUpTo<PayoutAccountDestinations.SelectPayoutMethod>(inclusive = true)
        },
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
