package com.hedvig.android.feature.connect.payment

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.connectPaymentGraph(navigator: Navigator) {
  navdestination<TrustlyDestination> {
    val viewModel: TrustlyViewModel = metroViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      finishTrustlyFlow = navigator::popBackStack,
    )
  }
}
