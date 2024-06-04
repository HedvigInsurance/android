package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.deleteAccountGraph(hedvigDeepLinkContainer: HedvigDeepLinkContainer, navigator: Navigator) {
  composable<DeleteAccountDestination>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.deleteAccount },
    ),
  ) {
    val viewModel: DeleteAccountViewModel = koinViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
}
