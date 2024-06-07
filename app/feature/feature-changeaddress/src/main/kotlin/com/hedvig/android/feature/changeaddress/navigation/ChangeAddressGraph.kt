package com.hedvig.android.feature.changeaddress.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressResultDestination
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.ChangeAddressEnterNewAddressDestination
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.ChangeAddressEnterVillaInformationDestination
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeViewModel
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.AddressResult
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.EnterNewAddress
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.EnterVillaInformation
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.Offer
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.AppDestination.ChangeAddress
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<AppDestination.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
  ) {
    composable<ChangeAddressDestination.SelectHousingType> { navBackStackEntry ->

      val viewModel: SelectHousingTypeViewModel = koinViewModel()
      ChangeAddressSelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateToEnterNewAddressDestination = { navController.navigate(EnterNewAddress) },
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
          navController.navigate(EnterVillaInformation)
        },
        navigateUp = navController::navigateUp,
        onNavigateToOfferDestination = {
          navController.navigate(Offer)
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
          navController.navigate(Offer)
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
          navController.navigate(AddressResult(movingDate)) {
            popUpTo<ChangeAddress> {
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
