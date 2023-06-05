package com.hedvig.android.feature.changeaddress.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.feature.changeaddress.navigation.ChangeAddressResultDestination
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popUpTo

fun NavGraphBuilder.changeAddressGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  openChat: () -> Unit,
  navigateUp: () -> Boolean,
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
        navigateUp = { navigateUp() },
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
        navController.navigateUp()
      }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        openChat = openChat,
        navigateBack = {
          viewModel.onQuotesCleared()
          navController.navigateUp()
        },
        onChangeAddressResult = {
          navController.navigate(ChangeAddressDestination.AddressResult) {
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
      navigateUp = navController::navigateUp,
      popBackstack = navController::popBackStack,
    )
  }
}
