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
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.travelCertificateGraph(
  backStack: MutableList<HedvigNavKey>,
  applicationId: String,
  onNavigateToCoInsuredAddInfo: (String) -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  navgraph(
    startDestination = TravelCertificateKey::class,
  ) {
    navdestination<TravelCertificateKey> {
      val viewModel: CertificateHistoryViewModel = metroViewModel()
      val localContext = LocalContext.current
      TravelCertificateHistoryDestination(
        viewModel = viewModel,
        navigateUp = backStack::navigateUp,
        onStartGenerateTravelCertificateFlow = {
          backStack.add(TravelCertificateDateInputKey(null))
        },
        onNavigateToChooseContract = {
          backStack.add(TravelCertificateChooseContractKey)
        },
        onShareTravelCertificate = {
          viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
          localContext.sharePDF(it, applicationId)
        },
        onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
      )
    }

    navdestination<TravelCertificateChooseContractKey> {
      val viewModel: ChooseContractForCertificateViewModel = metroViewModel()
      ChooseContractForCertificateDestination(
        viewModel = viewModel,
        onContinue = { contractId ->
          backStack.add(TravelCertificateDateInputKey(contractId))
        },
        navigateUp = backStack::navigateUp,
      )
    }

    navdestination<TravelCertificateDateInputKey> {
      val contractId = this.contractId
      val viewModel: TravelCertificateDateInputViewModel =
        assistedMetroViewModel<TravelCertificateDateInputViewModel, TravelCertificateDateInputViewModel.Factory> {
          create(contractId)
        }
      TravelCertificateDateInputDestination(
        viewModel = viewModel,
        navigateUp = backStack::navigateUp,
        onNavigateToFellowTravellers = { travelCertificatePrimaryInput ->
          backStack.add(
            TravelCertificateTravellersInputKey(
              travelCertificatePrimaryInput,
            ),
          )
        },
        onNavigateToOverview = { travelCertificateUrl ->
          backStack.navigateAndPopUpTo<TravelCertificateKey>(
            ShowCertificateKey(travelCertificateUrl),
            inclusive = false,
          )
        },
      )
    }

    navdestination<TravelCertificateTravellersInputKey> {
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
        navigateUp = backStack::navigateUp,
        onNavigateToOverview = { travelCertificateUrl ->
          backStack.navigateAndPopUpTo<TravelCertificateKey>(
            ShowCertificateKey(travelCertificateUrl),
            inclusive = false,
          )
        },
        onNavigateToCoInsuredAddInfo = { onNavigateToCoInsuredAddInfo(primaryInput.contractId) },
      )
    }

    navdestination<ShowCertificateKey> {
      val viewModel: TravelCertificateOverviewViewModel = metroViewModel()
      val context = LocalContext.current
      TravelCertificateOverviewDestination(
        travelCertificateUrl = travelCertificateUrl,
        viewModel = viewModel,
        navigateUp = backStack::navigateUp,
        onShareTravelCertificate = {
          context.sharePDF(it, applicationId)
        },
      )
    }
  }
}
