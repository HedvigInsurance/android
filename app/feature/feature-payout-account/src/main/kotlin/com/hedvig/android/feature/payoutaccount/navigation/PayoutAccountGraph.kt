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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.metroViewModel
import octopus.type.MemberPaymentProvider

fun EntryProviderScope<HedvigNavKey>.payoutAccountGraph(
  backstack: Backstack,
  globalSnackBarState: GlobalSnackBarState,
  navigateToConnectPayment: () -> Unit,
  navigateUp: () -> Unit,
) {
  entry<PayoutAccountKey> {
    val viewModel: PayoutAccountOverviewViewModel = metroViewModel()
    PayoutAccountOverviewDestination(
      viewModel = viewModel,
      onConnectPayoutMethodClicked = dropUnlessResumed {
        val content = viewModel.uiState.value as? PayoutAccountOverviewUiState.Content
        backstack.add(
          SelectPayoutMethodKey(
            availableProviders = content?.availablePayoutMethods?.map { it.rawValue } ?: emptyList(),
          ),
        )
      },
      navigateToConnectPayment = dropUnlessResumed {
        backstack.popUpTo<PayoutAccountKey>(inclusive = true)
        navigateToConnectPayment()
      },
      navigateUp = navigateUp,
    )
  }

  entry<SelectPayoutMethodKey> { key ->
    SelectPayoutMethodDestination(
      availableProviders = key.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
      onTrustlySelected = dropUnlessResumed {
        backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
        navigateToConnectPayment()
      },
      onNordeaSelected = dropUnlessResumed { backstack.add(EditBankAccountKey) },
      onSwishSelected = dropUnlessResumed { backstack.add(SetupSwishPayoutKey) },
      onInvoiceSelected = dropUnlessResumed { backstack.add(SetupInvoicePayoutKey) },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<EditBankAccountKey> {
    val viewModel: EditBankAccountViewModel = metroViewModel()
    EditBankAccountDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      onSuccessfullyConnected = {
        backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SetupSwishPayoutKey> {
    val viewModel: SetupSwishPayoutViewModel = metroViewModel()
    SetupSwishPayoutDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      onSuccessfullyConnected = {
        backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SetupInvoicePayoutKey> {
    val viewModel: SetupInvoicePayoutViewModel = metroViewModel()
    SetupInvoicePayoutDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      onSuccessfullyConnected = {
        backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
      },
      navigateUp = backstack::navigateUp,
    )
  }
}
