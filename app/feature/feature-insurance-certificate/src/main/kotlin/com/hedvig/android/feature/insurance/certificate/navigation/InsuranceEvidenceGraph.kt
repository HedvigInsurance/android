package com.hedvig.android.feature.insurance.certificate.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputDestination
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewDestination
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.core.common.android.sharePDF
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.insuranceEvidenceGraph(
  navigator: Navigator,
  applicationId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navgraph<InsuranceEvidenceGraphDestination>(
    startDestination = InsuranceEvidenceDestination.InsuranceEvidenceEmailInput::class,
  ) {
    navdestination<InsuranceEvidenceDestination.InsuranceEvidenceEmailInput>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.insuranceEvidence),
    ) { navBackStackEntry ->
      val viewModel: InsuranceEvidenceEmailInputViewModel = koinViewModel()
      InsuranceEvidenceEmailInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToShowCertificate = { url ->
          with(navigator) {
            navigateUnsafe(InsuranceEvidenceDestination.ShowCertificate(url)) {
              typedPopUpTo<InsuranceEvidenceDestination.InsuranceEvidenceEmailInput>({ inclusive = true })
            }
          }
        },
      )
    }
    navdestination<InsuranceEvidenceDestination.ShowCertificate> {
      val viewModel: InsuranceEvidenceOverviewViewModel = koinViewModel()
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
