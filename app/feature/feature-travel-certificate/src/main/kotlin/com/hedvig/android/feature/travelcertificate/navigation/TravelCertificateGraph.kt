package com.hedvig.android.feature.travelcertificate.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.ui.generate.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.generate.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.generate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.TravelCertificateHistoryDestination
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverview
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.travelCertificateGraph(density: Density, navController: NavController, applicationId: String) {
  navigation<AppDestination.TravelCertificate>(
    startDestination = createRoutePattern<TravelCertificateDestination.TravelCertificateHistory>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    composable<TravelCertificateDestination.TravelCertificateHistory> {
      val viewModel: CertificateHistoryViewModel = koinViewModel()
      val localContext = LocalContext.current
      TravelCertificateHistoryDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onStartGenerateTravelCertificateFlow = {
          navController.navigate(TravelCertificateDestination.GenerateTravelCertificateDestinations)
        },
        onShareTravelCertificate = {
          viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
          shareCertificate(it, localContext, applicationId)
        },
      )
    }
    navigation<TravelCertificateDestination.GenerateTravelCertificateDestinations>(
      startDestination = createRoutePattern<TravelCertificateDestination.TravelCertificateInput>(),
    ) {
      composable<TravelCertificateDestination.TravelCertificateInput> { navBackStackEntry ->
        val viewModel: GenerateTravelCertificateViewModel =
          destinationScopedViewModel<TravelCertificateDestination.GenerateTravelCertificateDestinations, _>(
            navController = navController,
            backStackEntry = navBackStackEntry,
          )

        val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()

        GenerateTravelCertificateInput(
          uiState = uiState,
          navigateBack = {
            navController.navigateUp()
          },
          onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
          onEmailChanged = viewModel::onEmailChanged,
          onCoInsuredClicked = { coInsured ->
            navController.navigate(TravelCertificateDestination.AddCoInsured(coInsured))
          },
          onAddCoInsuredClicked = {
            navController.navigate(TravelCertificateDestination.AddCoInsured(null))
          },
          onIncludeMemberClicked = viewModel::onIncludeMemberClicked,
          onTravelDateSelected = viewModel::onTravelDateSelected,
          onContinue = viewModel::onContinue,
          onSuccess = { travelCertificateUrl ->
            navController.navigate(TravelCertificateDestination.ShowCertificate(travelCertificateUrl)) {
              popUpTo<TravelCertificateDestination.GenerateTravelCertificateDestinations> {
                inclusive = true
              }
            }
          },
        )
      }
      composable<TravelCertificateDestination.AddCoInsured> { navBackStackEntry ->
        val viewModel: GenerateTravelCertificateViewModel =
          destinationScopedViewModel<TravelCertificateDestination.GenerateTravelCertificateDestinations, _>(
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
      composable<TravelCertificateDestination.ShowCertificate> { navBackStackEntry ->
        val viewModel: GenerateTravelCertificateViewModel =
          destinationScopedViewModel<TravelCertificateDestination.GenerateTravelCertificateDestinations, _>(
            navController = navController,
            backStackEntry = navBackStackEntry,
          )
        val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
        val localContext = LocalContext.current

        TravelCertificateOverview(
          travelCertificateUrl = travelCertificateUrl,
          onDownloadCertificate = {
            viewModel.onDownloadTravelCertificate(travelCertificateUrl)
          },
          travelCertificateUri = uiState.travelCertificateUri,
          isLoading = uiState.isLoading,
          errorMessage = uiState.errorMessage,
          onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
          navigateBack = navController::navigateUp,
          onShareTravelCertificate = {
            shareCertificate(it, localContext, applicationId)
          },
        )
      }
    }
  }
}

private fun shareCertificate(uri: TravelCertificateUri, context: Context, applicationId: String) {
  val contentUri = getUriForFile(context, "$applicationId.provider", uri.uri)

  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_VIEW
    setDataAndType(contentUri, "application/pdf")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  }
  val shareIntent = Intent.createChooser(sendIntent, "Hedvig Travel Certificate")
  context.startActivity(shareIntent)
}
