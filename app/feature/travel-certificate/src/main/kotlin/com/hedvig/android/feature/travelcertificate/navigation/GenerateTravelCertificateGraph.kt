package com.hedvig.android.feature.travelcertificate.navigation

import GenerateTravelCertificateViewModel
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.ui.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateInformation
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateOverView
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.ParametersDefinition


internal fun NavGraphBuilder.generateTravelCertificateGraph(
  density: Density,
  navController: NavHostController,
  finish: () -> Unit,
) {
  animatedNavigation<Destinations.GenerateTravelCertificate>(
    startDestination = createRoutePattern<GenerateTravelCertificateDestination.TravelCertificateInformation>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<GenerateTravelCertificateDestination.TravelCertificateInformation> {
      val viewModel = navGraphScopedViewModel(
        navController = navController,
        backStackEntry = it,
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
        navigateBack = { navController.navigateUp() },
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.TravelCertificateInput> {
      val viewModel = navGraphScopedViewModel(
        navController = navController,
        backStackEntry = it,
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
          if (viewModel.canAddCoInsured()) {
            navController.navigate(GenerateTravelCertificateDestination.AddCoInsured(null))
          } else {
            viewModel.onMaxCoInsureAdded()
          }
        },
        onRemoveCoInsuredClicked = { coInsured ->
          viewModel.onCoInsuredRemoved(coInsured.id)
        },
        onIncludeMemberClicked = viewModel::onIncludeMemberClicked,
        onTravelDateSelected = viewModel::onTravelDateSelected,
        onContinue = viewModel::onContinue,
        onSuccess = { travelCertificateUrl ->
          navController.navigate(GenerateTravelCertificateDestination.ShowCertificate(travelCertificateUrl))
        },
      )
    }
    animatedComposable<GenerateTravelCertificateDestination.AddCoInsured> {
      val viewModel = navGraphScopedViewModel(
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
    animatedComposable<GenerateTravelCertificateDestination.ShowCertificate> {
      val viewModel = navGraphScopedViewModel(
        navController = navController,
        backStackEntry = it,
      )
      val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
      val context = LocalContext.current

      BackHandler {
        finish()
      }

      TravelCertificateOverView(
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
          val contentUri = getUriForFile(context, "com.hedvig.fileprovider", it.uri)

          val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
          }
          val shareIntent = Intent.createChooser(sendIntent, "Hedvig Travel Certificate")
          context.startActivity(shareIntent)
        },
      )
    }
  }
}

@Composable
private fun navGraphScopedViewModel(
  navController: NavHostController,
  backStackEntry: NavBackStackEntry,
  parameters: ParametersDefinition? = null,
): GenerateTravelCertificateViewModel {
  val parentEntry = remember(navController, backStackEntry) {
    navController.getBackStackEntry(createRoutePattern<Destinations.GenerateTravelCertificate>())
  }
  return koinViewModel(
    viewModelStoreOwner = parentEntry,
    parameters = parameters,
  )
}
