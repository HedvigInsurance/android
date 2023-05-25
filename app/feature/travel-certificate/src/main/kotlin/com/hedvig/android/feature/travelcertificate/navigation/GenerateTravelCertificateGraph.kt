package com.hedvig.android.feature.travelcertificate.navigation

import GenerateTravelCertificateViewModel
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import com.hedvig.android.feature.travelcertificate.ui.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.generateTravelCertificateGraph(
  email: String?,
  travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  finish: () -> Unit,
) {
  animatedNavigation<Destinations.GenerateTravelCertificate>(
    startDestination = createRoutePattern<GenerateTravelCertificateDestination.TravelCertificateInput>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<GenerateTravelCertificateDestination.TravelCertificateInput> {
      val viewModel = navGraphScopedViewModel(
        email = email,
        travelCertificateSpecifications = travelCertificateSpecifications,
        navController = navController,
        backStackEntry = it,
      )

      val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
      GenerateTravelCertificateInput(
        uiState = uiState,
        navigateBack = { finish() },
        onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
        onEmailChanged = viewModel::onEmailChanged,
        onCoInsuredClicked = {
          navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(it))
        },
        onAddCoInsuredClicked = {
          if (viewModel.canAddCoInsured()) {
            navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(null))
          } else {
            viewModel.onMaxCoInsureAdded()
          }
        },
        onIncludeMemberClicked = viewModel::onIncludeMemberClicked,
        onTravelDateSelected = viewModel::onTravelDateSelected,
        onContinue = viewModel::onContinue,
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.AddCoInsured> {
      val viewModel = navGraphScopedViewModel(
        email = email,
        travelCertificateSpecifications = travelCertificateSpecifications,
        navController = navController,
        backStackEntry = it,
      )

      AddCoInsured(
        coInsured = coInsured,
        navigateBack = { navController.navigateUp() },
        onRemoveCoInsured = { coInsuredId ->
          navController.navigateUp()
          viewModel.onCoInsuredRemoved(coInsuredId)
        },
        onEditCoInsured = { coInsured ->
          navController.navigateUp()
          viewModel.onEditCoInsured(coInsured)
        },
        onAddCoInsured = {
          navController.navigateUp()
          viewModel.onAddCoInsured(it)
        },
      )
    }
  }
}

@Composable
private fun navGraphScopedViewModel(
  email: String?,
  travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
  navController: NavHostController,
  backStackEntry: NavBackStackEntry,
): GenerateTravelCertificateViewModel {
  val parentEntry = remember(navController, backStackEntry) {
    navController.getBackStackEntry(createRoutePattern<Destinations.GenerateTravelCertificate>())
  }
  return koinViewModel(viewModelStoreOwner = parentEntry) {
    parametersOf(email, travelCertificateSpecifications)
  }
}
