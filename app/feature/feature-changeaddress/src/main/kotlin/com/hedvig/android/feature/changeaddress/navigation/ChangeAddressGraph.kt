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
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<AppDestination.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
  ) {
    composable<ChangeAddressDestination.SelectHousingType> { navBackStackEntry ->
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

    composable<ChangeAddressDestination.EnterNewAddress> { navBackStackEntry ->
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

    composable<ChangeAddressDestination.EnterVillaInformation> { navBackStackEntry ->
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

    composable<ChangeAddressDestination.Offer> { backStackEntry ->
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
  composable<ChangeAddressDestination.AddressResult> {
    ChangeAddressResultDestination(
      movingDate = movingDate,
      popBackstack = navController::popBackStack,
    )
  }
}
