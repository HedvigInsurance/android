package com.hedvig.android.feature.partner.claim.details.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.partner.claim.details.ui.PartnerClaimDetailsDestination
import com.hedvig.android.feature.partner.claim.details.ui.PartnerClaimDetailsViewModel
import com.hedvig.android.navigation.compose.navdestination
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.partnerClaimDetailsGraph(navigateUp: () -> Unit, openUrl: (String) -> Unit) {
  navdestination<PartnerClaimOverviewDestination> {
    val viewModel: PartnerClaimDetailsViewModel = koinViewModel { parametersOf(claimId) }
    PartnerClaimDetailsDestination(
      viewModel = viewModel,
      navigateUp = navigateUp,
      openUrl = openUrl,
    )
  }
}
