package com.feature.changeaddress.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.feature.changeaddress.ui.ChangeAddressEnterNewDestination
import com.feature.changeaddress.ChangeAddressViewModel
import com.feature.changeaddress.ui.ChangeAddressOfferDestination
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.changeAddressGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  navigateUp: () -> Boolean,
  finish: () -> Unit,
) {
  animatedNavigation<Destinations.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.EnterNewAddress>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ChangeAddressDestination.EnterNewAddress> {
      val viewModel: ChangeAddressViewModel = koinViewModel()
      ChangeAddressEnterNewDestination(
        viewModel = viewModel,
        navigateBack = { navigateUp() },
        onQuotes = { quotes ->
          navController.navigate(ChangeAddressDestination.MoveQuotes(quotes))
        },
      )
    }

    animatedComposable<ChangeAddressDestination.MoveQuotes> {
      ChangeAddressOfferDestination(
        quotes,
      )
    }
  }
}
