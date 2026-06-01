package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.MANUAL_CHARGE_CANCELLATION_WARNING
import hedvig.resources.PAYMENTS_PAYMENT_IN_PROGRESS
import hedvig.resources.PAYMENTS_PAYMENT_IN_PROGRESS_DESCRIPTION
import hedvig.resources.Res
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualChargeSuccessDestination(showCancellationWarning: Boolean, popBackStack: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(
        WindowInsets.safeDrawing.only(
          WindowInsetsSides.Horizontal +
            WindowInsetsSides.Bottom,
        ),
      ),
  ) {
    Spacer(Modifier.weight(1f))
    EmptyState(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(Res.string.PAYMENTS_PAYMENT_IN_PROGRESS),
      description = stringResource(
        Res.string.PAYMENTS_PAYMENT_IN_PROGRESS_DESCRIPTION,
      ),
      iconStyle = if (showCancellationWarning) {
        EmptyStateDefaults.EmptyStateIconStyle.SUCCESS_WITH_WARNING
      } else {
        SUCCESS
      },
      buttonStyle = NoButton,
    )
    Column(Modifier.weight(1f)) {
      if (showCancellationWarning) {
        Spacer(Modifier.height(16.dp))
        HedvigNotificationCard(
          message = stringResource(Res.string.MANUAL_CHARGE_CANCELLATION_WARNING),
          priority = NotificationDefaults.NotificationPriority.AttentionRound,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
    HedvigButton(
      stringResource(Res.string.general_close_button),
      onClick = dropUnlessResumed { popBackStack() },
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun ManualChargeSuccessDestinationPreview(
  @PreviewParameter(
    BooleanCollectionPreviewParameterProvider::class,
  ) showCancellationWarning: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ManualChargeSuccessDestination(
        showCancellationWarning,
        {},
      )
    }
  }
}
