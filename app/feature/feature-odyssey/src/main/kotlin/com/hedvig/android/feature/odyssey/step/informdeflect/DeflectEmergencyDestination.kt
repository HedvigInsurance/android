package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.runtime.Composable
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.ui.emergency.EmergencyScreen

@Composable
internal fun DeflectEmergencyDestination(
  deflectEmergency: ClaimFlowDestination.DeflectEmergency,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  DeflectEmergencyScreen(
    partners = deflectEmergency.partners,
    navigateUp = navigateUp,
    openUrl = openUrl,
  )
}

@Composable
private fun DeflectEmergencyScreen(partners: List<DeflectPartner>, navigateUp: () -> Unit, openUrl: (String) -> Unit) {
  EmergencyScreen(
    emergencyNumber = partners.firstNotNullOfOrNull { it.phoneNumber },
    emergencyUrl = partners.firstNotNullOfOrNull { it.url },
    navigateUp = navigateUp,
    openUrl = openUrl,
    preferredPartnerImageHeight = partners.firstNotNullOfOrNull { it.preferredImageHeight },
  )
}

@HedvigPreview
@Composable
private fun DeflectEmergencyScreenPreview() {
  DeflectEmergencyScreen(
    partners = listOf(
      DeflectPartner(
        id = "1",
        imageUrl = "test",
        phoneNumber = "1234",
        url = "test",
        preferredImageHeight = null,
      ),
    ),
    navigateUp = {},
    openUrl = {},
  )
}
