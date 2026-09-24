package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.provider
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.payin.account.data.SwishSetupOrder
import com.hedvig.android.feature.payin.account.data.id
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsDestination
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModel
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModelFactory
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewUiState
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodViewModelFactory
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodViewModelFactory
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusViewModelFactory
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.compose.removeAllOf
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.payinAccountEntries(
  backstack: Backstack,
  globalSnackBarState: GlobalSnackBarState,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToTrustly: () -> Unit,
  openUrl: (String) -> Unit,
) {
  entry<PayinAccountKey> {
    val viewModel: PayinAccountOverviewViewModel = metroViewModel()
    PayinAccountOverviewDestination(
      viewModel = viewModel,
      onConnectPayinMethodClicked = dropUnlessResumed {
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
      navigateBack = backstack::popBackstack,
      onChangeMethod = dropUnlessResumed { method: PayinAccount ->
        when (method) {
          is PayinAccount.Trustly -> {
            backstack.popUpTo<PayinMethodDetailsKey>(inclusive = true)
            navigateToTrustly()
          }

          is PayinAccount.SwishPayin -> {
            backstack.add(SetupSwishPayinKey())
          }

          // Invoice has no setup flow of its own, so the details screen offers no change
          // button for it and this never fires.
          is PayinAccount.Invoice -> {}
        }
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
    val viewModel: SelectPayinMethodViewModel =
      assistedMetroViewModel<SelectPayinMethodViewModel, SelectPayinMethodViewModelFactory> {
        create(key.availableProviders, key.currentProviders)
      }
    SelectPayinMethodDestination(
      viewModel = viewModel,
      onTrustlySelected = dropUnlessResumed {
        backstack.popUpTo<SelectPayinMethodKey>(inclusive = true)
        navigateToTrustly()
      },
      onSwishSelected = dropUnlessResumed { backstack.add(SetupSwishPayinKey()) },
      navigateUp = backstack::navigateUp,
    )
  }

  // Leaves the whole Swish flow behind: both of its screens, plus the method picker when the member
  // came through one, so a connected member never lands back inside the flow they just finished.
  val finishSwishSetup: () -> Unit = {
    backstack.popUpTo<SetupSwishPayinKey>(inclusive = true)
    backstack.removeAllOf<SelectPayinMethodKey>()
  }

  entry<SetupSwishPayinKey> { key ->
    val viewModel: SetupSwishPayinViewModel = metroViewModel()
    SetupSwishPayinDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      onSuccessfullyConnected = finishSwishSetup,
      navigateToApproval = dropUnlessResumed { order: SwishSetupOrder, phoneNumber: String ->
        backstack.add(
          SwishPayinStatusKey(order.successUrl, order.orderId, phoneNumber, key.showSuccessScreen),
        )
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SwishPayinStatusKey> { key ->
    val viewModel: SwishPayinStatusViewModel =
      assistedMetroViewModel<SwishPayinStatusViewModel, SwishPayinStatusViewModelFactory> {
        create(key.successUrl, key.orderId, key.phoneNumber)
      }
    SwishPayinStatusDestination(
      viewModel = viewModel,
      // Staging orders can only be approved in the Swish sandbox app, never the real one.
      allowSandboxSwishApp = !hedvigBuildConstants.isProduction,
      showSuccessScreen = key.showSuccessScreen,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      finishSwishSetup = finishSwishSetup,
      // Back to the picker, leaving the number behind with the attempt that failed.
      changePaymentMethod = { backstack.popUpTo<SetupSwishPayinKey>(inclusive = true) },
      openUrl = openUrl,
    )
  }
}
