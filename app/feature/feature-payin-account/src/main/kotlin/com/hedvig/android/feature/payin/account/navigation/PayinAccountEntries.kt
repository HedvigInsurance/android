package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewUiState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.metroViewModel
import octopus.type.MemberPaymentProvider

fun EntryProviderScope<HedvigNavKey>.payinAccountEntries(
  backstack: Backstack,
  globalSnackBarState: GlobalSnackBarState,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
) {
  entry<PayinAccountKey> {
    val viewModel: PayinAccountOverviewViewModel = metroViewModel()
    PayinAccountOverviewDestination(
      viewModel = viewModel,
      onConnectPayoutMethodClicked = dropUnlessResumed {
        val content = viewModel.uiState.value as? PayinAccountOverviewUiState.Content
        backstack.add(
          SelectPayinMethodKey(
            availableProviders = content?.availablePayinMethods?.map { it.rawValue } ?: emptyList(),
          ),
        )
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SelectPayinMethodKey> { key ->
    SelectPayinMethodDestination(
      availableProviders = key.availableProviders.map { MemberPaymentProvider.safeValueOf(it) },
      onTrustlySelected = dropUnlessResumed {
        backstack.popUpTo<SelectPayinMethodKey>(inclusive = true)
        navigateToConnectPayment()
      },
      onSwishSelected = dropUnlessResumed { backstack.add(SetupSwishPayinKey) },
      onInvoiceSelected = dropUnlessResumed { backstack.add(SetupInvoicePayinKey) },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SetupSwishPayinKey> {
    val viewModel: SetupSwishPayinViewModel = metroViewModel()
    SetupSwishPayinDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      onSuccessfullyConnected = { backstack.popUpTo<SelectPayinMethodKey>(inclusive = true) },
      navigateUp = backstack::navigateUp,
      openUrl = {
        backstack.popUpTo<SelectPayinMethodKey>(inclusive = true)
        openUrl(it)
      },
    )
  }

  entry<SetupInvoicePayinKey> {
    val viewModel: SetupInvoicePayinViewModel = metroViewModel()
    SetupInvoicePayinDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      navigateUp = backstack::navigateUp,
    )
  }
}
