package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.ui.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.mockUiState
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate

internal fun NavGraphBuilder.generateTravelCertificateGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  navigateUp: () -> Boolean,
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
      GenerateTravelCertificateInput(
        uiState = mockUiState,
        navigateBack = {
          finish()
        },
        onErrorDialogDismissed = {},
        onEmailChanged = {},
        onCoInsuredClicked = {
          navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(it))
        },
        onAddCoInsuredClicked = {
          navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(null))
        },
        onIncludeMemberClicked = {},
        onTravelDateSelected = {},
        onContinue = {},
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.AddCoInsured> {
      AddCoInsured(
        selectedCoInsuredId = id,
        uiState = mockUiState,
        navigateBack = {
          navController.navigateUp()
        },
        onSsnChanged = {},
        onNameChanged = {},
        onRemoveClicked = {
          navController.navigateUp()           
        },
        onSaveClicked = {
          navController.navigateUp()
        },
      )
    }
  }
}
