package com.hedvig.android.feature.changeaddress.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewAddressDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterVillaInformationDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressResultDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<AppDestination.ChangeAddress>(
    startDestination = ChangeAddressDestination.SelectHousingType::class,
  ) {
    navdestination<ChangeAddressDestination.SelectHousingType> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressSelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateToEnterNewAddressDestination = { navController.navigate(ChangeAddressDestination.EnterNewAddress) },
      )
    }

    navdestination<ChangeAddressDestination.EnterNewAddress> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressEnterNewAddressDestination(
        viewModel = viewModel,
        onNavigateToVillaInformationDestination = {
          navController.navigate(ChangeAddressDestination.EnterVillaInformation)
        },
        navigateUp = navController::navigateUp,
        onNavigateToOfferDestination = {
          navController.navigate(ChangeAddressDestination.Offer)
        },
      )
    }

    navdestination<ChangeAddressDestination.EnterVillaInformation> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressEnterVillaInformationDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onNavigateToOfferDestination = {
          navController.navigate(ChangeAddressDestination.Offer)
        },
      )
    }

    navdestination<ChangeAddressDestination.Offer> { backStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = backStackEntry,
      )
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        openChat = { openChat(backStackEntry) },
        navigateUp = navController::navigateUp,
        onChangeAddressResult = { movingDate ->
          navController.navigate(ChangeAddressDestination.AddressResult(movingDate)) {
            popUpTo<AppDestination.ChangeAddress> {
              inclusive = true
            }
          }
        },
        openUrl = openUrl,
      )
    }
  }
  navdestination<ChangeAddressDestination.AddressResult>(
    typeMap = ChangeAddressDestination.AddressResult.typeMap,
  ) { navBackStackEntry ->
    ChangeAddressResultDestination(
      movingDate = movingDate,
      popBackstack = navController::popBackStack,
    )
  }
}
