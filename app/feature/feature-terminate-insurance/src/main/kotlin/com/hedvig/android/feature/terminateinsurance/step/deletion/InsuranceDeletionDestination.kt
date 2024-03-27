package com.hedvig.android.feature.terminateinsurance.step.deletion

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardInsurance
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold

@Composable
internal fun InsuranceDeletionDestination(
  displayName: String,
  exposureName: String,
  closeTerminationFlow: () -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) {
    Text(
      text = "Confirm your cancellation", // todo: real copy here
      fontSize = MaterialTheme.typography.headlineSmall.fontSize,
      fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
      fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    TerminationInfoCardInsurance(
      displayName = displayName,
      exposureName = exposureName,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    TerminationInfoCardDate(
      dateValue = "Today", // todo: actual copy here
      onClick = {},
      isLocked = true,
    )
    Spacer(modifier = Modifier.height(8.dp))
    VectorInfoCard(
      text = "Since this insurance is not active yet it will be cancelled and removed today. You won’t be charged anything.",
      // todo: actual copy here
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = "Cancel insurance", // todo: actual copy here
      onClick = onContinue,
      enabled = true,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDeletionScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceDeletionDestination(
        displayName = "Homeowner insurance",
        exposureName = "Bellmansgatan 19",
        onContinue = {},
        closeTerminationFlow = {},
        navigateUp = {},
      )
    }
  }
}
