package com.hedvig.android.feature.travelcertificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.travelCertificateGraph(
  navigator: Navigator,
  applicationId: String,
  onNavigateToCoInsuredAddInfo: (String) -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  navgraph(
    startDestination = TravelCertificateGraphDestination::class,
  ) {
    navdestination<TravelCertificateGraphDestination> {
      val viewModel: CertificateHistoryViewModel = metroViewModel()
      val localContext = LocalContext.current
      TravelCertificateHistoryDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onStartGenerateTravelCertificateFlow = {
          navigator.navigate(TravelCertificateDestination.TravelCertificateDateInput(null))
        },
        onNavigateToChooseContract = {
          navigator.navigate(TravelCertificateDestination.TravelCertificateChooseContract)
        },
        onShareTravelCertificate = {
          viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
          localContext.sharePDF(it, applicationId)
        },
        onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateChooseContract> {
      val viewModel: ChooseContractForCertificateViewModel = metroViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          navigator.navigate(TravelCertificateDestination.TravelCertificateDateInput(contractId))
        },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateDateInput> {
      val contractId = this.contractId
      val viewModel: TravelCertificateDateInputViewModel =
        assistedMetroViewModel<TravelCertificateDateInputViewModel, TravelCertificateDateInputViewModel.Factory> {
          create(contractId)
        }
      TravelCertificateDateInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToFellowTravellers = { travelCertificatePrimaryInput ->
          navigator.navigate(
            TravelCertificateDestination.TravelCertificateTravellersInput(
              travelCertificatePrimaryInput,
            ),
          )
        },
        onNavigateToOverview = { travelCertificateUrl ->
          navigator.navigate<TravelCertificateGraphDestination>(
            TravelCertificateDestination.ShowCertificate(travelCertificateUrl),
            inclusive = false,
          )
        },
      )
    }

    navdestination<TravelCertificateDestination.TravelCertificateTravellersInput> {
      val primaryInput = this.primaryInput
      val viewModel: TravelCertificateTravellersInputViewModel =
        assistedMetroViewModel<
          TravelCertificateTravellersInputViewModel,
          TravelCertificateTravellersInputViewModel.Factory,
        > {
          create(primaryInput)
        }
      TravelCertificateTravellersInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToOverview = { travelCertificateUrl ->
          navigator.navigate<TravelCertificateGraphDestination>(
            TravelCertificateDestination.ShowCertificate(travelCertificateUrl),
            inclusive = false,
          )
        },
        onNavigateToCoInsuredAddInfo = { onNavigateToCoInsuredAddInfo(primaryInput.contractId) },
      )
    }

    navdestination<TravelCertificateDestination.ShowCertificate> {
      val viewModel: TravelCertificateOverviewViewModel = metroViewModel()
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
