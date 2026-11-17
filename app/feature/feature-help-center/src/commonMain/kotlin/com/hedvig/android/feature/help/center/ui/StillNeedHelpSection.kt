package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.minimumInteractiveComponentSize
import hedvig.resources.CHAT_NEW_MESSAGE
import hedvig.resources.HC_CHAT_ANSWER
import hedvig.resources.HC_CHAT_GO_TO_INBOX
import hedvig.resources.HC_CHAT_QUESTION
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StillNeedHelpSection(
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  Surface(
    modifier = modifier,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding),
    ) {
      Spacer(modifier = Modifier.height(32.dp))
      Icon(
        imageVector = HedvigIcons.InfoFilled,
        tint = HedvigTheme.colorScheme.signalBlueElement,
        contentDescription = "Info icon",
        modifier = Modifier
          .wrapContentSize(Alignment.CenterEnd)
          .size(40.dp)
          .clip(HedvigTheme.shapes.cornerXLarge)
          .minimumInteractiveComponentSize(),
      )
      Spacer(modifier = Modifier.height(24.dp))
      HedvigText(
        text = stringResource(Res.string.HC_CHAT_QUESTION),
        textAlign = TextAlign.Center,
      )
      HedvigText(
        text = stringResource(Res.string.HC_CHAT_ANSWER),
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        style = LocalTextStyle.current.copy(
          lineBreak = LineBreak.Heading,
        ),
      )
      Spacer(modifier = Modifier.height(40.dp))
      Row {
        HedvigButton(
          text = stringResource(Res.string.CHAT_NEW_MESSAGE),
          onClick = onNavigateToNewConversation,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        HedvigButton(
          text = stringResource(Res.string.HC_CHAT_GO_TO_INBOX),
          onClick = onNavigateToInbox,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Primary,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
          modifier = Modifier.weight(1f),
        )
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewStillNeedHelpSection() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      StillNeedHelpSection({}, {})
    }
  }
}
