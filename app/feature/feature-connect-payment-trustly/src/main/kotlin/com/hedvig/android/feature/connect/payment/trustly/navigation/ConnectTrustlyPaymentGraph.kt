package com.hedvig.android.feature.connect.payment

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.connectPaymentGraph(backStack: MutableList<HedvigNavKey>) {
  entry<TrustlyKey> {
    val viewModel: TrustlyViewModel = metroViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      finishTrustlyFlow = backStack::popBackStack,
    )
  }
}
