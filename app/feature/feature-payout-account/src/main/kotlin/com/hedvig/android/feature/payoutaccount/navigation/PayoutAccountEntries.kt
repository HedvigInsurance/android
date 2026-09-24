package com.hedvig.android.feature.payoutaccount.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountDestination
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountViewModel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewDestination
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewViewModel
import com.hedvig.android.feature.payoutaccount.ui.selectmethod.SelectPayoutMethodDestination
import com.hedvig.android.feature.payoutaccount.ui.setupswish.SetupSwishPayoutDestination
import com.hedvig.android.feature.payoutaccount.ui.setupswish.SetupSwishPayoutViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.payoutAccountEntries(backstack: Backstack, navigateToTrustly: () -> Unit) {
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
      navigateToTrustly = dropUnlessResumed {
        backstack.popUpTo<PayoutAccountKey>(inclusive = true)
        navigateToTrustly()
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SelectPayoutMethodKey> { key ->
    SelectPayoutMethodDestination(
      availableProviders = key.availableProviders.mapNotNull { PaymentProvider.fromRawValue(it) },
      onTrustlySelected = dropUnlessResumed {
        backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
        navigateToTrustly()
      },
      onNordeaSelected = dropUnlessResumed { backstack.add(EditBankAccountKey) },
      onSwishSelected = dropUnlessResumed { backstack.add(SetupSwishPayoutKey) },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<EditBankAccountKey> {
    val viewModel: EditBankAccountViewModel = metroViewModel()
    EditBankAccountDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SetupSwishPayoutKey> {
    val viewModel: SetupSwishPayoutViewModel = metroViewModel()
    SetupSwishPayoutDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }
}
