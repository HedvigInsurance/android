package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_BODY
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_CONTINUE
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_START_NEW
import hedvig.resources.RESUME_CLAIM_DRAFT_ALERT_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
fun DraftClaimDialog(
  onDismissRequest: () -> Unit,
  onContinueDraft: () -> Unit,
  onStartNewClaim: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Two-button dialog: left "Continue draft" (safe), right "Start new claim" (destructive, red).
  // SMALL keeps them side by side when they fit and stacks them otherwise. Dismissing via scrim or
  // back does nothing beyond closing; both actions are wired to the explicit buttons.
  HedvigDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    style = Buttons(
      onDismissRequest = onContinueDraft,
      dismissButtonText = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_CONTINUE),
      onConfirmButtonClick = onStartNewClaim,
      confirmButtonText = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_START_NEW),
      confirmButtonRedText = true,
    ),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_TITLE),
        textAlign = TextAlign.Center,
      )
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_BODY),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDraftClaimDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DraftClaimDialog({}, {}, {})
    }
  }
}
