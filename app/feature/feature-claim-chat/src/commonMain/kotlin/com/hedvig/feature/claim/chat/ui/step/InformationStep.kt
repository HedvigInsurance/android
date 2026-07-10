package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.feature.claim.chat.data.StepContent

@Composable
internal fun InformationStep(
  information: StepContent.Information,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  onAcknowledge: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    HedvigNotificationCard(
      message = information.notice,
      priority = when (information.severity) {
        StepContent.Information.Severity.INFO -> NotificationPriority.Info
        StepContent.Information.Severity.CRITICAL -> NotificationPriority.Error
      },
    )
    if (isCurrentStep) {
      HedvigButton(
        text = information.buttonTitle,
        onClick = onAcknowledge,
        isLoading = continueButtonLoading,
        enabled = !continueButtonLoading,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInformationStep(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isCritical: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InformationStep(
        information = StepContent.Information(
          notice = "Since your home is currently uninhabitable and you have nowhere to stay, please contact us " +
            "immediately or seek temporary emergency accommodation.",
          severity = if (isCritical) {
            StepContent.Information.Severity.CRITICAL
          } else {
            StepContent.Information.Severity.INFO
          },
          buttonTitle = "I understand",
        ),
        isCurrentStep = true,
        continueButtonLoading = false,
        onAcknowledge = {},
      )
    }
  }
}
