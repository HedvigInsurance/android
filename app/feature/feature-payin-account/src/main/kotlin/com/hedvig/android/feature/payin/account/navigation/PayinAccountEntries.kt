package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.id
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsDestination
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModel
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModelFactory
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewUiState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodViewModelFactory
import com.hedvig.android.feature.payin.account.ui.selectmethod.ConnectPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.selectmethod.ConnectPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.selectmethod.ConnectPayinMethodViewModelFactory
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinDestination
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

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
            currentProviders = content?.currentMethods?.map { it.provider.rawValue } ?: emptyList(),
          ),
        )
      },
      onPayinMethodClicked = dropUnlessResumed { method: PayinAccount ->
        backstack.add(PayinMethodDetailsKey(method.id))
      },
      onChoosePrimaryMethodClicked = dropUnlessResumed {
        val content = viewModel.uiState.value as? PayinAccountOverviewUiState.Content
        backstack.add(SelectPrimaryPayinMethodKey(currentMethods = content?.currentMethods ?: emptyList()))
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<PayinMethodDetailsKey> { key ->
    val viewModel: PayinMethodDetailsViewModel =
      assistedMetroViewModel<PayinMethodDetailsViewModel, PayinMethodDetailsViewModelFactory> {
        create(key.method)
      }
    PayinMethodDetailsDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onChangeMethod = dropUnlessResumed { method: PayinAccount ->
        when (method) {
          is PayinAccount.Trustly -> {
            backstack.popUpTo<PayinMethodDetailsKey>(inclusive = true)
            navigateToConnectPayment()
          }

          is PayinAccount.SwishPayin -> {
            backstack.add(SetupSwishPayinKey)
          }

          is PayinAccount.Invoice -> {
            backstack.add(SetupInvoicePayinKey)
          }
        }
      },
      onRemoveMethod = {
        // TODO: call the remove-payin-method API once the backend exposes one.
      },
    )
  }

  entry<SelectPrimaryPayinMethodKey> { key ->
    val viewModel: SelectPrimaryPayinMethodViewModel =
      assistedMetroViewModel<SelectPrimaryPayinMethodViewModel, SelectPrimaryPayinMethodViewModelFactory> {
        create(key.currentMethods)
      }
    SelectPrimaryPayinMethodDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }

  entry<SelectPayinMethodKey> { key ->
    val viewModel: ConnectPayinMethodViewModel =
      assistedMetroViewModel<ConnectPayinMethodViewModel, ConnectPayinMethodViewModelFactory> {
        create(key.availableProviders, key.currentProviders)
      }
    ConnectPayinMethodDestination(
      viewModel = viewModel,
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
