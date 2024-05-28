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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardInsurance
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

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
      style = MaterialTheme.typography.headlineSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      text = stringResource(id = R.string.TERMINATION_FLOW_CONFIRM_INFORMATION),
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
      dateValue = stringResource(id = R.string.TERMINATION_FLOW_TODAY),
      onClick = null,
      isLocked = true,
    )
    Spacer(modifier = Modifier.height(8.dp))
    VectorInfoCard(
      text = stringResource(id = R.string.TERMINATION_FLOW_DELETION_INFO_CARD),
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCEL_INSURANCE_BUTTON),
      onClick = onContinue,
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
