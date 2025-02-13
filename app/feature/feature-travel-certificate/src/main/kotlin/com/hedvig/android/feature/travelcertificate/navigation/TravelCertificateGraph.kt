package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.common.android.sharePDF
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
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.travelCertificateGraph(
  navigator: Navigator,
  applicationId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToCoInsuredAddInfo: (String) -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  navgraph<TravelCertificateGraphDestination>(
    startDestination = TravelCertificateDestination.TravelCertificateHistory::class,
  ) {
    navdestination<TravelCertificateDestination.TravelCertificateHistory>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.travelCertificate),
    ) { navBackStackEntry ->
      val viewModel: CertificateHistoryViewModel = koinViewModel()
      val localContext = LocalContext.current
      TravelCertificateHistoryDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onStartGenerateTravelCertificateFlow = {
          with(navigator) {
            navBackStackEntry.navigate(TravelCertificateDestination.TravelCertificateDateInput(null))
          }
        },
        onNavigateToChooseContract = {
          with(navigator) {
            navBackStackEntry.navigate(TravelCertificateDestination.TravelCertificateChooseContract)
          }
        },
        onShareTravelCertificate = {
          viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
          localContext.sharePDF(it, applicationId)
        },
        onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateChooseContract> { navBackStackEntry ->
      val viewModel: ChooseContractForCertificateViewModel = koinViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          with(navigator) {
            navBackStackEntry.navigate(TravelCertificateDestination.TravelCertificateDateInput(contractId))
          }
        },
        navigateUp = navigator::navigateUp,
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
        navigateUp = navigator::navigateUp,
        onNavigateToFellowTravellers = { travelCertificatePrimaryInput ->
          navigator.navigateUnsafe(
            TravelCertificateDestination.TravelCertificateTravellersInput(
              travelCertificatePrimaryInput,
            ),
          )
        },
        onNavigateToOverview = { travelCertificateUrl ->
          navigator.navigateUnsafe(
            TravelCertificateDestination.ShowCertificate(travelCertificateUrl),
          ) {
            typedPopUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateTravellersInput>(
      TravelCertificateDestination.TravelCertificateTravellersInput,
    ) { navBackStackEntry ->
      val viewModel: TravelCertificateTravellersInputViewModel = koinViewModel(
        parameters = {
          parametersOf(primaryInput)
        },
      )
      TravelCertificateTravellersInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToOverview = { travelCertificateUrl ->
          navigator.navigateUnsafe(
            TravelCertificateDestination.ShowCertificate(travelCertificateUrl),
          ) {
            typedPopUpTo<TravelCertificateDestination.TravelCertificateHistory> {
              inclusive = false
            }
          }
        },
        onNavigateToCoInsuredAddInfo = { onNavigateToCoInsuredAddInfo(primaryInput.contractId) },
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
        navigateUp = navigator::navigateUp,
        onShareTravelCertificate = {
          context.sharePDF(it, applicationId)
        },
      )
    }
  }
}
