package com.hedvig.android.feature.help.center.commonclaim.emergency

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.ui.emergency.EmergencyScreen

@Composable
internal fun EmergencyDestination(emergencyData: CommonClaim.Emergency, navigateUp: () -> Unit) {
  EmergencyScreen(
    title = emergencyData.title,
    emergencyNumber = emergencyData.emergencyNumber,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  )
}
