package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.runtime.Composable
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.ui.emergency.EmergencyScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DeflectEmergencyDestination(
  deflectEmergency: ClaimFlowDestination.DeflectEmergency,
  navigateUp: () -> Unit,
) {
  DeflectEmergencyScreen(
    partners = deflectEmergency.partners,
    navigateUp = navigateUp,
  )
}

@Composable
private fun DeflectEmergencyScreen(partners: ImmutableList<DeflectPartner>, navigateUp: () -> Unit) {
  EmergencyScreen(
    emergencyNumber = partners.firstNotNullOfOrNull { it.phoneNumber },
    navigateUp = navigateUp,
  )
}

@HedvigPreview
@Composable
private fun DeflectEmergencyScreenPreview() {
  DeflectEmergencyScreen(
    partners = persistentListOf(
      DeflectPartner(
        id = "1",
        imageUrl = "test",
        phoneNumber = "1234",
        url = "test",
      ),
    ),
    navigateUp = {},
  )
}
