package com.hedvig.android.feature.payin.account.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payin.account.data.id
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsDestination
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModel
import com.hedvig.android.feature.payin.account.ui.methoddetails.PayinMethodDetailsViewModelFactory
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewDestination
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodDestination
import com.hedvig.android.feature.payin.account.ui.selectmethod.SelectPayinMethodViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusDestination
import com.hedvig.android.feature.payin.account.ui.setupswish.SwishPayinStatusViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.compose.removeAllOf
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.payinAccountEntries(
  backstack: Backstack,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToTrustly: () -> Unit,
  /**
   * Leaves a payin setup that was not opened from the method picker, landing on the payin overview.
   * A lone deep link has nothing under it, so this builds the overview's ancestry rather than popping.
   */
  returnToPayinOverview: () -> Unit,
) {
  entry<PayinAccountKey> {
    val viewModel: PayinAccountOverviewViewModel = metroViewModel()
    PayinAccountOverviewDestination(
      viewModel = viewModel,
      onConnectPayinMethodClicked = dropUnlessResumed { backstack.add(SelectPayinMethodKey) },
      onPayinMethodClicked = dropUnlessResumed { method: PayinAccount ->
        backstack.add(PayinMethodDetailsKey(method.id))
      },
      onChoosePrimaryMethodClicked = dropUnlessResumed { backstack.add(SelectPrimaryPayinMethodKey) },
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
            backstack.navigateAndPopUpTo<PayinMethodDetailsKey>(SetupSwishPayinKey(), inclusive = true)
          }

          // Invoice has no setup flow of its own, so the details screen offers no change
          // button for it and this never fires.
          is PayinAccount.Invoice -> {}
        }
      },
    )
  }

  entry<SelectPrimaryPayinMethodKey> {
    val viewModel: SelectPrimaryPayinMethodViewModel = metroViewModel()
    SelectPrimaryPayinMethodDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }

  entry<SelectPayinMethodKey> {
    val viewModel: SelectPayinMethodViewModel = metroViewModel()
    SelectPayinMethodDestination(
      viewModel = viewModel,
      onTrustlySelected = dropUnlessResumed {
        backstack.popUpTo<SelectPayinMethodKey>(inclusive = true)
        navigateToTrustly()
      },
      onSwishSelected = dropUnlessResumed { backstack.add(SetupSwishPayinKey(openedFromPicker = true)) },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SetupSwishPayinKey> { key ->
    val viewModel: SwishPayinStatusViewModel = assistedMetroViewModel()
    SwishPayinStatusDestination(
      viewModel = viewModel,
      // Staging orders can only be approved in the Swish sandbox app, never the real one.
      allowSandboxSwishApp = !hedvigBuildConstants.isProduction,
      showSuccessScreen = key.showSuccessScreen,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      // Leaves the Swish setup behind, plus the method picker when the member came through one, so a
      // connected member never lands back inside the flow they just finished.
      finishSwishSetup = {
        if (key.openedFromPicker) {
          backstack.popUpTo<SetupSwishPayinKey>(inclusive = true)
          backstack.removeAllOf<SelectPayinMethodKey>()
        } else {
          returnToPayinOverview()
        }
      },
      changePaymentMethod = {
        if (key.openedFromPicker) {
          backstack.popUpTo<SetupSwishPayinKey>(inclusive = true)
        } else {
          returnToPayinOverview()
          if (backstack.entries.lastOrNull() is PayinAccountKey) {
            backstack.add(SelectPayinMethodKey)
          }
        }
      },
    )
  }
}
