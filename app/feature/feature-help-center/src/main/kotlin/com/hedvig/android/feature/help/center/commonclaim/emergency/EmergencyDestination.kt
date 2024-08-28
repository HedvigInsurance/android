package com.hedvig.android.feature.help.center.commonclaim.emergency

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.ui.emergency.EmergencyScreen

@Composable
internal fun EmergencyDestination(
  emergencyNumber: String?,
  emergencyUrl: String?,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  EmergencyScreen(
    emergencyNumber = emergencyNumber,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
    openUrl = openUrl,
    emergencyUrl = emergencyUrl,
  )
}
