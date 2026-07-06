package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
  HedvigDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    Column {
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_TITLE),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_BODY),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_CONTINUE),
        onClick = onContinueDraft,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(Res.string.RESUME_CLAIM_DRAFT_ALERT_START_NEW),
        onClick = onStartNewClaim,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Red,
        modifier = Modifier.fillMaxWidth(),
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
