package com.hedvig.android.feature.changeaddress.navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewVillaAddressDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressResultDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  openChat: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  navigation<AppDestination.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
    deepLinks = listOf(),
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
      BackHandler {
        navController.popBackStack<AppDestination.ChangeAddress>(inclusive = true)
      }
      ChangeAddressEnterNewDestination(
        viewModel = viewModel,
        onContinue = { navController.navigate(ChangeAddressDestination.EnterNewVillaAddress) },
        close = { navController.popBackStack<ChangeAddressDestination.SelectHousingType>(inclusive = true) },
        onQuotesReceived = {
          navController.navigate(ChangeAddressDestination.OfferDestination)
        },
      )
    }

    composable<ChangeAddressDestination.EnterNewVillaAddress> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressEnterNewVillaAddressDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onQuotesReceived = {
          navController.navigate(ChangeAddressDestination.OfferDestination)
        },
      )
    }

    composable<ChangeAddressDestination.OfferDestination> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      BackHandler {
        viewModel.onQuotesCleared()
        navController.popBackStack<AppDestination.ChangeAddress>(inclusive = true)
      }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        openChat = openChat,
        close = {
          viewModel.onQuotesCleared()
          navController.popBackStack<AppDestination.ChangeAddress>(inclusive = true)
        },
        onChangeAddressResult = { movingDate ->
          navController.navigate(ChangeAddressDestination.AddressResult(movingDate)) {
            popUpTo<AppDestination.ChangeAddress> {
              inclusive = true
            }
          }
        },
        openUrl = openUrl,
        imageLoader = imageLoader,
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
