package com.hedvig.android.feature.inapppurchase.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormDestination
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormViewModel
import com.hedvig.android.feature.inapppurchase.ui.form.InAppPurchaseFormViewModelFactory
import com.hedvig.android.feature.inapppurchase.ui.offers.InAppPurchaseOffersDestination
import com.hedvig.android.feature.inapppurchase.ui.offers.InAppPurchaseOffersViewModel
import com.hedvig.android.feature.inapppurchase.ui.offers.InAppPurchaseOffersViewModelFactory
import com.hedvig.android.feature.inapppurchase.ui.success.InAppPurchaseSuccessScreen
import com.hedvig.android.feature.inapppurchase.ui.summary.InAppPurchaseSummaryDestination
import com.hedvig.android.feature.inapppurchase.ui.summary.InAppPurchaseSummaryViewModel
import com.hedvig.android.feature.inapppurchase.ui.summary.InAppPurchaseSummaryViewModelFactory
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.inAppPurchaseEntries(
  backstack: Backstack,
  navigateToContract: (contractId: String) -> Unit,
  finishPurchase: () -> Unit,
) {
  val exitFlow: () -> Unit = { backstack.popUpTo<InAppPurchaseKey>(inclusive = true) }

  entry<InAppPurchaseKey> { key ->
    val product = key.product
    val viewModel: InAppPurchaseFormViewModel =
      assistedMetroViewModel<InAppPurchaseFormViewModel, InAppPurchaseFormViewModelFactory> {
        create(product)
      }
    InAppPurchaseFormDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      popBackstack = { backstack.popBackstack() },
      navigateToContract = navigateToContract,
    )
  }

  entry<InAppPurchaseOffersKey> { key ->
    val viewModel: InAppPurchaseOffersViewModel =
      assistedMetroViewModel<InAppPurchaseOffersViewModel, InAppPurchaseOffersViewModelFactory> {
        create(key)
      }
    InAppPurchaseOffersDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<InAppPurchaseSummaryKey> { key ->
    val viewModel: InAppPurchaseSummaryViewModel =
      assistedMetroViewModel<InAppPurchaseSummaryViewModel, InAppPurchaseSummaryViewModelFactory> {
        create(key)
      }
    InAppPurchaseSummaryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      exitFlow = exitFlow,
    )
  }

  entry<InAppPurchaseSuccessKey> { key ->
    InAppPurchaseSuccessScreen(
      startDate = key.startDate,
      close = finishPurchase,
    )
  }
}
