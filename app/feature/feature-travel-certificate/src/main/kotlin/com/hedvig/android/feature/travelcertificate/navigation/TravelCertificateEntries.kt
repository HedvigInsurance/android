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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.travelCertificateEntries(
  backstack: Backstack,
  applicationId: String,
  onNavigateToCoInsuredAddInfo: (String) -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  entry<TravelCertificateKey> {
    val viewModel: CertificateHistoryViewModel = metroViewModel()
    val localContext = LocalContext.current
    TravelCertificateHistoryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onStartGenerateTravelCertificateFlow = {
        backstack.add(TravelCertificateDateInputKey(null))
      },
      onNavigateToChooseContract = {
        backstack.add(TravelCertificateChooseContractKey)
      },
      onShareTravelCertificate = {
        viewModel.emit(CertificateHistoryEvent.HaveProcessedCertificateUri)
        localContext.sharePDF(it, applicationId)
      },
      onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
    )
  }

  entry<TravelCertificateChooseContractKey> {
    val viewModel: ChooseContractForCertificateViewModel = metroViewModel()
    ChooseContractForCertificateDestination(
      viewModel = viewModel,
      onContinue = { contractId ->
        backstack.add(TravelCertificateDateInputKey(contractId))
      },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<TravelCertificateDateInputKey> { key ->
    val contractId = key.contractId
    val viewModel: TravelCertificateDateInputViewModel =
      assistedMetroViewModel<TravelCertificateDateInputViewModel, TravelCertificateDateInputViewModel.Factory> {
        create(contractId)
      }
    TravelCertificateDateInputDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<TravelCertificateTravellersInputKey> { key ->
    val primaryInput = key.primaryInput
    val viewModel: TravelCertificateTravellersInputViewModel =
      assistedMetroViewModel<
        TravelCertificateTravellersInputViewModel,
        TravelCertificateTravellersInputViewModel.Factory,
      > {
        create(primaryInput)
      }
    TravelCertificateTravellersInputDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onNavigateToCoInsuredAddInfo = { onNavigateToCoInsuredAddInfo(primaryInput.contractId) },
    )
  }

  entry<ShowCertificateKey> { key ->
    val viewModel: TravelCertificateOverviewViewModel = metroViewModel()
    val context = LocalContext.current
    TravelCertificateOverviewDestination(
      travelCertificateUrl = key.travelCertificateUrl,
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onShareTravelCertificate = {
        context.sharePDF(it, applicationId)
      },
    )
  }
}
