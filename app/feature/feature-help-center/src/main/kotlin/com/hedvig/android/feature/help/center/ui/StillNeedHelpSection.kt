package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import hedvig.resources.R

@Composable
internal fun StillNeedHelpSection(
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  showNavigateToInboxButton: Boolean,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding)
        .padding(horizontal = 16.dp),
    ) {
      Spacer(modifier = Modifier.height(32.dp))
      Text(
        text = stringResource(R.string.HC_CHAT_QUESTION),
        textAlign = TextAlign.Center,
      )
      Text(
        text = stringResource(R.string.HC_CHAT_ANSWER),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(16.dp))
      if (showNavigateToInboxButton) {
        HedvigButton(
          text = stringResource(R.string.HC_CHAT_GO_TO_INBOX),
          onClick = onNavigateToInbox,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Primary,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
        Spacer(Modifier.height(8.dp))
      }
      HedvigButton(
        text = stringResource(R.string.HC_CHAT_BUTTON),
        onClick = onNavigateToNewConversation,
        enabled = true,
        buttonStyle = if (showNavigateToInboxButton) {
          ButtonDefaults.ButtonStyle.Ghost
        } else {
          ButtonDefaults.ButtonStyle.Primary
        },
        buttonSize = ButtonDefaults.ButtonSize.Medium,
      )
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewStillNeedHelpSection(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) showNavigateToInboxButton: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      StillNeedHelpSection({}, {}, showNavigateToInboxButton)
    }
  }
}
