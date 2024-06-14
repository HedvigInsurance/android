package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
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
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.AppDestination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.travelCertificateGraph(density: Density, navController: NavController, applicationId: String) {
  navgraph<AppDestination.TravelCertificate>(
    startDestination = TravelCertificateDestination.TravelCertificateHistory::class,
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    navdestination<TravelCertificateDestination.TravelCertificateHistory> {
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

    navdestination<TravelCertificateDestination.TravelCertificateChooseContract> {
      val viewModel: ChooseContractForCertificateViewModel = koinViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          navController.navigate(TravelCertificateDestination.TravelCertificateDateInput(contractId))
        },
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateDateInput> {
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
            typedPopUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateTravellersInput>(
      TravelCertificateDestination.TravelCertificateTravellersInput,
    ) {
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
            typedPopUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
        onNavigateToCoInsuredAddInfo = {
          navController.navigate(AppDestination.CoInsuredAddInfo(primaryInput.contractId))
        },
      )
    }

    navdestination<TravelCertificateDestination.ShowCertificate>(
      TravelCertificateDestination.ShowCertificate,
    ) {
      val viewModel: TravelCertificateOverviewViewModel = koinViewModel()
      val context = LocalContext.current
      TravelCertificateOverviewDestination(
        travelCertificateUrl = travelCertificateUrl,
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onShareTravelCertificate = {
          context.sharePDF(it, applicationId)
        },
      )
    }
  }
}
