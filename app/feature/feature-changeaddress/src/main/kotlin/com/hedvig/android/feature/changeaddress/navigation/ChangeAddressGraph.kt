package com.hedvig.android.feature.changeaddress.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.Density
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressResultDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo

fun NavGraphBuilder.changeAddressGraph(
  density: Density,
  navController: NavController,
  openChat: () -> Unit,
) {
  animatedNavigation<AppDestination.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ChangeAddressDestination.SelectHousingType> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressSelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onHousingTypeSubmitted = {
          navController.navigate(ChangeAddressDestination.EnterNewAddress)
        },
      )
    }

    animatedComposable<ChangeAddressDestination.EnterNewAddress> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      ChangeAddressEnterNewDestination(
        viewModel = viewModel,
        navigateBack = { navController.navigateUp() },
        onQuotesReceived = {
          navController.navigate(ChangeAddressDestination.OfferDestination)
        },
      )
    }

    animatedComposable<ChangeAddressDestination.OfferDestination> { navBackStackEntry ->
      val viewModel: ChangeAddressViewModel = destinationScopedViewModel<AppDestination.ChangeAddress, _>(
        navController = navController,
        backStackEntry = navBackStackEntry,
      )
      BackHandler {
        viewModel.onQuotesCleared()
        navController.popBackStack<ChangeAddressDestination.SelectHousingType>(inclusive = true)
      }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        openChat = openChat,
        close = {
          viewModel.onQuotesCleared()
          navController.popBackStack<ChangeAddressDestination.SelectHousingType>(inclusive = true)
        },
        onChangeAddressResult = { movingDate ->
          navController.navigate(ChangeAddressDestination.AddressResult(movingDate)) {
            popUpTo<AppDestination.ChangeAddress> {
              inclusive = true
            }
          }
        },
      )
    }
  }
  animatedComposable<ChangeAddressDestination.AddressResult> {
    ChangeAddressResultDestination(
      movingDate = movingDate,
      popBackstack = navController::popBackStack,
    )
  }
}
