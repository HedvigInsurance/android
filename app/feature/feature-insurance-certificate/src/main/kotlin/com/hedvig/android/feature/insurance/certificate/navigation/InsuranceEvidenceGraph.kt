package com.hedvig.android.feature.insurance.certificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputDestination
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewDestination
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<Destination>.insuranceEvidenceGraph(navigator: Navigator, applicationId: String) {
  navgraph(
    startDestination = InsuranceEvidenceGraphDestination::class,
  ) {
    navdestination<InsuranceEvidenceGraphDestination> {
      val viewModel: InsuranceEvidenceEmailInputViewModel = metroViewModel()
      InsuranceEvidenceEmailInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToShowCertificate = { url ->
          navigator.navigate<InsuranceEvidenceGraphDestination>(
            InsuranceEvidenceDestination.ShowCertificate(url),
            inclusive = true,
          )
        },
      )
    }
    navdestination<InsuranceEvidenceDestination.ShowCertificate> {
      val viewModel: InsuranceEvidenceOverviewViewModel = metroViewModel()
      val context = LocalContext.current
      InsuranceEvidenceOverviewDestination(
        insuranceEvidenceUrl = certificateUrl,
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onShareInsuranceEvidence = {
          context.sharePDF(it, applicationId)
        },
      )
    }
  }
}
