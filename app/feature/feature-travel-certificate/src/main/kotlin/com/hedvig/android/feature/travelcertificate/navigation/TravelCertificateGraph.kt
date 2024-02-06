package com.hedvig.android.feature.travelcertificate.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateViewModel
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.ui.AddCoInsured
import com.hedvig.android.feature.travelcertificate.ui.GenerateTravelCertificateInput
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateHistoryDestination
import com.hedvig.android.feature.travelcertificate.ui.TravelCertificateOverview
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.typed.destinationScopedViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popBackStack
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.travelCertificateGraph(
  density: Density,
  navController: NavController,
  applicationId: String,
  finish: () -> Unit = {
    navController.popBackStack<TravelCertificateDestination.TravelCertificateHistory>(false)
  },
) {
  navigation<AppDestination.TravelCertificate>(
    startDestination = createRoutePattern<TravelCertificateDestination.TravelCertificateHistory>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    composable<TravelCertificateDestination.TravelCertificateHistory> {
      val viewModel: CertificateHistoryViewModel = koinViewModel()
      TravelCertificateHistoryDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onContinue = { navController.navigate(TravelCertificateDestination.GenerateTravelCertificateDestinations) },
      )
    }
    navigation<TravelCertificateDestination.GenerateTravelCertificateDestinations>(
      startDestination = createRoutePattern<TravelCertificateDestination.TravelCertificateInput>(),
      enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
      exitTransition = { MotionDefaults.sharedXAxisExit(density) },
      popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
      popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
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
            navController.navigate(TravelCertificateDestination.ShowCertificate(travelCertificateUrl))
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

        BackHandler {
          finish()
//        navController.navigate(GenerateTravelCertificateDestination.TravelCertificateHistory)
//        navController.popBackStack<AppDestination.TravelCertificate>(false)
//        // todo: how
//        // todo: check here.
//        // todo: what exactly happens to backstack here, do we need to create AppDestination that inherits from
//        // todo: Destination
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
//        navigateBack = {
//          navController.navigate(GenerateTravelCertificateDestination.TravelCertificateHistory)
//        }, // todo: check here}
          onShareTravelCertificate = {
            val contentUri = getUriForFile(localContext, "$applicationId.provider", it.uri)

            val sendIntent: Intent = Intent().apply {
              action = Intent.ACTION_VIEW
              setDataAndType(contentUri, "application/pdf")
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Hedvig Travel Certificate")
            localContext.startActivity(shareIntent)
          },
        )
      }
    }
  }
}

internal fun openWebsite(context: Context, uri: Uri) {
  val browserIntent = Intent(Intent.ACTION_VIEW, uri)
  if (browserIntent.resolveActivity(context.packageManager) != null) {
    context.startActivity(browserIntent)
  } else {
    logcat(LogPriority.ERROR) { "Tried to launch $uri but the phone has nothing to support such an intent." }
  }
}
