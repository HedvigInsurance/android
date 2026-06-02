package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.deleteAccountGraph(backStack: MutableList<HedvigNavKey>) {
  entry<DeleteAccountKey> {
    val viewModel: DeleteAccountViewModel = metroViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
    )
  }
}
