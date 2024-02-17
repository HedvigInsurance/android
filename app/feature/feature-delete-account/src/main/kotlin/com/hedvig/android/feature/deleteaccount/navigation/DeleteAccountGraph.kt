package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.deleteAccountGraph(navigator: Navigator) {
  composable<DeleteAccountDestination> {
    val viewModel: DeleteAccountViewModel = koinViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
}
