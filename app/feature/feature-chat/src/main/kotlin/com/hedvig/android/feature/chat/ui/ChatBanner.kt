package com.hedvig.android.feature.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewFontScale
import com.halilibo.richtext.commonmark.Markdown
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun ChatBanner(
  text: String,
  possibleToClose: Boolean,
  modifier: Modifier = Modifier) {
  var visible by remember { mutableStateOf(true) }
  AnimatedVisibility(visible,
    modifier = modifier) {
    ProvideTextStyle(HedvigTheme.typography.label.copy(color = HedvigTheme.colorScheme.signalBlueText)) {
      HedvigNotificationCard(
        content = {
          RichText {
            Markdown(
              content = text,
            )
          }
        },
        priority = Info,
        modifier = Modifier
          .fillMaxWidth()
          .background(Info.colors.containerColor),
        withIcon = true,
        style = if (possibleToClose) NotificationDefaults.InfoCardStyle.Button(
          buttonText = stringResource(R.string.general_close_button),
          onButtonClick = {
            visible = false
          }
        ) else Default,
      )
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun PreviewChatBanner() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChatBanner("HHHHHH".repeat(15),
        possibleToClose = true)
    }
  }
}
