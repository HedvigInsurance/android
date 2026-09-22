package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.horizontalDivider
import hedvig.resources.CLAIM_CHAT_AI_INFO
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AiDisclaimerCard(modifier: Modifier = Modifier) {
  // The one fill the UI kit does not carry: in light mode the white is held at 70% so the label keeps its
  // contrast ratio against the chat background behind it.
  val background = if (isSystemInDarkTheme()) {
    HedvigTheme.colorScheme.backgroundBlack
  } else {
    HedvigTheme.colorScheme.backgroundWhite.copy(alpha = 0.7f)
  }
  HedvigText(
    text = stringResource(Res.string.CLAIM_CHAT_AI_INFO),
    style = HedvigTheme.typography.label,
    color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    modifier = modifier
      .fillMaxWidth()
      .background(background)
      .horizontalDivider(DividerPosition.Bottom, color = HedvigTheme.colorScheme.borderPrimary)
      .padding(start = 16.dp, top = 11.dp, end = 16.dp, bottom = 13.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewAiDisclaimerCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AiDisclaimerCard()
    }
  }
}
