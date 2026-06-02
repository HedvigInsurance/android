package com.hedvig.android.feature.insurance.certificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputDestination
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewDestination
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.insuranceEvidenceGraph(
  backStack: MutableList<HedvigNavKey>,
  applicationId: String,
) {
  navdestination<InsuranceEvidenceKey> {
    val viewModel: InsuranceEvidenceEmailInputViewModel = metroViewModel()
    InsuranceEvidenceEmailInputDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateToShowCertificate = { url ->
        backStack.navigateAndPopUpTo<InsuranceEvidenceKey>(
          ShowCertificateKey(url),
          inclusive = true,
        )
      },
    )
  }
  navdestination<ShowCertificateKey> {
    val viewModel: InsuranceEvidenceOverviewViewModel = metroViewModel()
    val context = LocalContext.current
    InsuranceEvidenceOverviewDestination(
      insuranceEvidenceUrl = certificateUrl,
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onShareInsuranceEvidence = {
        context.sharePDF(it, applicationId)
      },
    )
  }
}
