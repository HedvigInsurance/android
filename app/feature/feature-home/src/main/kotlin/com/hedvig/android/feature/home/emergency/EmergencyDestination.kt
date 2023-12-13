package com.hedvig.android.feature.home.emergency

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.ui.emergency.EmergencyScreen

@Composable
internal fun EmergencyDestination(emergencyData: EmergencyData, navigateUp: () -> Unit) {
  EmergencyScreen(
    title = emergencyData.title,
    emergencyNumber = emergencyData.emergencyNumber,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  )
}
