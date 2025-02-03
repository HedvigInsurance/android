package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.deleteAccountGraph(hedvigDeepLinkContainer: HedvigDeepLinkContainer, navigator: Navigator) {
  navdestination<DeleteAccountDestination>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.deleteAccount),
  ) {
    val viewModel: DeleteAccountViewModel = koinViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
}
