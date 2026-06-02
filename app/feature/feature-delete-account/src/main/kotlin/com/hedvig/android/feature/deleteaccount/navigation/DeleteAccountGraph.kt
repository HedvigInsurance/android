package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.deleteAccountGraph(navigator: Navigator) {
  navdestination<DeleteAccountDestination> {
    val viewModel: DeleteAccountViewModel = metroViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
}
