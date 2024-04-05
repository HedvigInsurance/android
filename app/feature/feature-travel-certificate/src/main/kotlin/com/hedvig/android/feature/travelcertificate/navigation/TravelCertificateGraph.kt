package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateDestination
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputDestination
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputDestination
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryEvent
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.feature.travelcertificate.ui.history.TravelCertificateHistoryDestination
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewDestination
import com.hedvig.android.feature.travelcertificate.ui.overview.TravelCertificateOverviewViewModel
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.travelCertificateGraph(density: Density, navController: NavController, applicationId: String) {
  navigation<AppDestination.TravelCertificate>(
    startDestination = TravelCertificateDestination.TravelCertificateHistory::class,
  ) {
    composable<TravelCertificateDestination.TravelCertificateHistory> { _ ->
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
          localContext.sharePDF(it, applicationId)
        },
      )
    }

    composable<TravelCertificateDestination.TravelCertificateChooseContract> { _ ->
      val viewModel: ChooseContractForCertificateViewModel = koinViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          navController.navigate(TravelCertificateDestination.TravelCertificateDateInput(contractId))
        },
        navigateUp = navController::navigateUp,
      )
    }

    composable<TravelCertificateDestination.TravelCertificateDateInput> { _, destination ->
      val viewModel: TravelCertificateDateInputViewModel = koinViewModel(
        parameters = {
          parametersOf(destination.contractId)
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

    composable<TravelCertificateDestination.TravelCertificateTravellersInput> { _, destination ->
      val viewModel: TravelCertificateTravellersInputViewModel = koinViewModel(
        parameters = {
          parametersOf(destination.primaryInput)
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
        onNavigateToCoInsuredAddInfo = {
          navController.navigate(AppDestination.CoInsuredAddInfo(destination.primaryInput.contractId))
        },
      )
    }

    composable<TravelCertificateDestination.ShowCertificate> { _, destination ->
      val viewModel: TravelCertificateOverviewViewModel = koinViewModel()
      val context = LocalContext.current
      TravelCertificateOverviewDestination(
        travelCertificateUrl = destination.travelCertificateUrl,
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onShareTravelCertificate = {
          context.sharePDF(it, applicationId)
        },
      )
    }
  }
}
