package com.hedvig.android.feature.insurance.certificate.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.fileupload.rememberOpenPdf
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputDestination
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewDestination
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.insuranceEvidenceEntries(backstack: Backstack, applicationId: String) {
  entry<InsuranceEvidenceKey> {
    val viewModel: InsuranceEvidenceEmailInputViewModel = metroViewModel()
    InsuranceEvidenceEmailInputDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }
  entry<ShowCertificateKey> { key ->
    val viewModel: InsuranceEvidenceOverviewViewModel = metroViewModel()
    val openPdf = rememberOpenPdf(applicationId)
    InsuranceEvidenceOverviewDestination(
      insuranceEvidenceUrl = key.certificateUrl,
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onShareInsuranceEvidence = openPdf,
    )
  }
}
