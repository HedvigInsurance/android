package com.hedvig.android.feature.insurance.certificate.navigation

import androidx.navigation.NavGraphBuilder
import androidx.compose.ui.platform.LocalContext
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputDestination
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
              navBackStackEntry.navigate(InsuranceEvidenceDestination.ShowCertificate(url))
            }
          }
        )
    }
    navdestination<InsuranceEvidenceDestination.ShowCertificate> {
        val viewModel: InsuranceEvidenceOverviewViewModel = koinViewModel()
        val context = LocalContext.current
//        InsuranceEvidenceOverviewDestination(
//          certificateUrl = certificateUrl,
//          viewModel = viewModel,
//          navigateUp = navigator::navigateUp,
//          onShareTravelCertificate = {
//            context.sharePDF(it, applicationId)
//          },
//        )
    }
  }
}



