package com.hedvig.android.feature.connect.payment

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackstack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.connectPaymentGraph(backstack: Backstack) {
  entry<TrustlyKey> {
    val viewModel: TrustlyViewModel = metroViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      finishTrustlyFlow = backstack::popBackstack,
    )
  }
}
