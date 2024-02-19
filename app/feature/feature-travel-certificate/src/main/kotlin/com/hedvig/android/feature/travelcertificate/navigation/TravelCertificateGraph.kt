package com.hedvig.android.feature.travelcertificate.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateDestination
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generate_when.TravelCertificateDateInputDestination
import com.hedvig.android.feature.travelcertificate.ui.generate_when.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generate_who.TravelCertificateTravellersInputDestination
import com.hedvig.android.feature.travelcertificate.ui.generate_who.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.TravelCertificateHistoryDestination
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewDestination
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
          navController.navigate(TravelCertificateDestination.TravelCertificateDateInput(null))
        },
        onNavigateToChooseContract = {
          navController.navigate(TravelCertificateDestination.TravelCertificateChooseContract)
        },
        onShareTravelCertificate = {
          viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
          shareCertificate(it, localContext, applicationId)
        },
      )
    }

    composable<TravelCertificateDestination.TravelCertificateChooseContract> {
      val viewModel: ChooseContractForCertificateViewModel = koinViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          navController.navigate(TravelCertificateDestination.TravelCertificateDateInput(contractId))
        },
        navigateUp = navController::navigateUp,
      )
    }

    composable<TravelCertificateDestination.TravelCertificateDateInput> {
      val viewModel: TravelCertificateDateInputViewModel = koinViewModel(
        parameters = {
          parametersOf(contractId)
        },
      )
      TravelCertificateDateInputDestination(
        viewModel = viewModel,
        navigateUp = { navController.navigateUp() },
        onNavigateToFellowTravellers = { travelCertificatePrimaryInput ->
          navController.navigate(
            TravelCertificateDestination.TravelCertificateTravellersInput(
              travelCertificatePrimaryInput,
            ),
          )
        },
        onNavigateToOverview = { travelCertificateUrl ->
          navController.navigate(TravelCertificateDestination.ShowCertificate(travelCertificateUrl)) {
            popUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
      )
    }

    composable<TravelCertificateDestination.TravelCertificateTravellersInput> {
      val viewModel: TravelCertificateTravellersInputViewModel = koinViewModel(
        parameters = {
          parametersOf(primaryInput)
        },
      )
      TravelCertificateTravellersInputDestination(
        viewModel = viewModel,
        navigateUp = { navController.navigateUp() },
        onNavigateToOverview = { travelCertificateUrl ->
          navController.navigate(TravelCertificateDestination.ShowCertificate(travelCertificateUrl)) {
            popUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
      )
    }

//
//    navigation<TravelCertificateDestination.GenerateTravelCertificateDestinations>(
//      startDestination = createRoutePattern<TravelCertificateDestination.TravelCertificateInput>(),
//    ) {
//      composable<TravelCertificateDestination.TravelCertificateInput> { navBackStackEntry ->
//        val viewModel: GenerateTravelCertificateViewModel =
//          destinationScopedViewModel<TravelCertificateDestination.GenerateTravelCertificateDestinations, _>(
//            navController = navController,
//            backStackEntry = navBackStackEntry,
//            parameters = {
//              parametersOf(contractId)
//            },
//          )
//        val uiState: TravelCertificateInputState by viewModel.uiState.collectAsStateWithLifecycle()
//        GenerateTravelCertificateInput(
//          uiState = uiState,
//          navigateBack = {
//            navController.navigateUp()
//          },
//          onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
//          onEmailChanged = viewModel::onEmailChanged,
//          onCoInsuredClicked = { coInsured ->
//            navController.navigate(TravelCertificateDestination.AddCoInsured(coInsured, contractId))
//          },
//          onAddCoInsuredClicked = {
//            navController.navigate(TravelCertificateDestination.AddCoInsured(null, contractId))
//          },
//          onIncludeMemberClicked = viewModel::onIncludeMemberClicked,
//          onTravelDateSelected = viewModel::onTravelDateSelected,
//          onContinue = viewModel::onContinue,
//          onSuccess = { travelCertificateUrl ->
//            navController.navigate(TravelCertificateDestination.ShowCertificate(travelCertificateUrl)) {
//              popUpTo<TravelCertificateDestination.TravelCertificateHistory> {
//                inclusive = false
//              }
//            }
//          },
//        )
//      }
//      composable<TravelCertificateDestination.AddCoInsured> { navBackStackEntry ->
//        val viewModel: GenerateTravelCertificateViewModel =
//          destinationScopedViewModel<TravelCertificateDestination.GenerateTravelCertificateDestinations, _>(
//            navController = navController,
//            backStackEntry = navBackStackEntry,
//            parameters = { parametersOf(contractId) },
//          )
//
//        AddCoInsured(
//          coInsured = coInsured,
//          navigateUp = navController::navigateUp,
//          onRemoveCoInsured = { coInsuredId ->
//            navController.navigateUp()
//            viewModel.onCoInsuredRemoved(coInsuredId)
//          },
//          onEditCoInsured = { coInsured ->
//            navController.navigateUp()
//            viewModel.onEditCoInsured(coInsured)
//          },
//          onAddCoInsured = {
//            navController.navigateUp()
//            viewModel.onAddCoInsured(it)
//          },
//        )
//      }
//    }

    composable<TravelCertificateDestination.ShowCertificate> {
      val viewModel: TravelCertificateOverviewViewModel = koinViewModel()
      val context = LocalContext.current
      TravelCertificateOverviewDestination(
        travelCertificateUrl = travelCertificateUrl,
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onShareTravelCertificate = {
          shareCertificate(it, context, applicationId)
        },
      )
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
