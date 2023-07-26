package com.hedvig.android.feature.travelcertificate.navigation

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.ui.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateInformation
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateOverview
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack

fun NavGraphBuilder.generateTravelCertificateGraph(
  density: Density,
  navController: NavController,
  applicationId: String,
  finish: () -> Unit = {
    navController.popBackStack<AppDestination.GenerateTravelCertificate>(true)
  },
) {
  animatedNavigation<AppDestination.GenerateTravelCertificate>(
    startDestination = createRoutePattern<GenerateTravelCertificateDestination.TravelCertificateInformation>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<GenerateTravelCertificateDestination.TravelCertificateInformation> { navBackStackEntry ->
      val viewModel: GenerateTravelCertificateViewModel =
        destinationScopedViewModel<AppDestination.GenerateTravelCertificate, _>(
          navController = navController,
          backStackEntry = navBackStackEntry,
        )
      val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
      TravelCertificateInformation(
        isLoading = uiState.isLoading,
        infoSections = uiState.infoSections,
        errorMessage = uiState.errorMessage,
        onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
        onContinue = {
          navController.navigate(GenerateTravelCertificateDestination.TravelCertificateInput)
        },
        navigateUp = navController::navigateUp,
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.TravelCertificateInput> { navBackStackEntry ->
      val viewModel: GenerateTravelCertificateViewModel =
        destinationScopedViewModel<AppDestination.GenerateTravelCertificate, _>(
          navController = navController,
          backStackEntry = navBackStackEntry,
        )

      val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()

      GenerateTravelCertificateInput(
        uiState = uiState,
        navigateBack = { navController.navigateUp() },
        onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
        onEmailChanged = viewModel::onEmailChanged,
        onCoInsuredClicked = { coInsured ->
          navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(coInsured))
        },
        onAddCoInsuredClicked = {
          navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(null))
        },
        onIncludeMemberClicked = viewModel::onIncludeMemberClicked,
        onTravelDateSelected = viewModel::onTravelDateSelected,
        onContinue = viewModel::onContinue,
        onSuccess = { travelCertificateUrl ->
          navController.navigate(GenerateTravelCertificateDestination.ShowCertificate(travelCertificateUrl))
        },
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.AddCoInsured> { navBackStackEntry ->
      val viewModel: GenerateTravelCertificateViewModel =
        destinationScopedViewModel<AppDestination.GenerateTravelCertificate, _>(
          navController = navController,
          backStackEntry = navBackStackEntry,
        )

      AddCoInsured(
        coInsured = coInsured,
        navigateUp = navController::navigateUp,
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
    animatedComposable<GenerateTravelCertificateDestination.ShowCertificate> { navBackStackEntry ->
      val viewModel: GenerateTravelCertificateViewModel =
        destinationScopedViewModel<AppDestination.GenerateTravelCertificate, _>(
          navController = navController,
          backStackEntry = navBackStackEntry,
        )
      val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
      val context = LocalContext.current

      BackHandler {
        finish()
      }

      TravelCertificateOverview(
        travelCertificateUrl = travelCertificateUrl,
        onDownloadCertificate = {
          viewModel.onDownloadTravelCertificate(travelCertificateUrl)
        },
        travelCertificateUri = uiState.travelCertificateUri,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
        navigateBack = finish,
        onShareTravelCertificate = {
          val contentUri = getUriForFile(context, "$applicationId.provider", it.uri)

          val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          }
          val shareIntent = Intent.createChooser(sendIntent, "Hedvig Travel Certificate")
          context.startActivity(shareIntent)
        },
      )
    }
  }
}
