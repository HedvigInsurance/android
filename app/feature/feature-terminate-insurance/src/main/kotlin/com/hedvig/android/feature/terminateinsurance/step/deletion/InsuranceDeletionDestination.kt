package com.hedvig.android.feature.terminateinsurance.step.deletion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
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
    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        color = HedvigTheme.colorScheme.textSecondary,
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
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigNotificationCard(
      message = stringResource(id = R.string.TERMINATION_FLOW_DELETION_INFO_CARD),
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
      priority = NotificationPriority.Info,
    )
    Spacer(Modifier.height(16.dp))
    Row(
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth(),
    ) {
      HedvigButton(
        text = stringResource(id = R.string.TERMINATION_FLOW_CANCEL_INSURANCE_BUTTON),
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        enabled = true,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDeletionScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
