package com.hedvig.android.feature.deleteaccount.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountDestination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun NavGraphBuilder.deleteAccountGraph(hedvigDeepLinkContainer: HedvigDeepLinkContainer, navController: NavController) {
  navdestination<DeleteAccountDestination>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.deleteAccount),
  ) {
    val viewModel: DeleteAccountViewModel = metroViewModel()
    DeleteAccountDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      navigateBack = navController::popBackStack,
    )
  }
}
