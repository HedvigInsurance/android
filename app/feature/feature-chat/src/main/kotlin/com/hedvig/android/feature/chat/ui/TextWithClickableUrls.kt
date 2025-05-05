package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import com.halilibo.richtext.commonmark.Markdown
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RichText

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun TextWithClickableUrls(text: String, modifier: Modifier = Modifier, style: TextStyle = TextStyle.Default) {
  ProvideTextStyle(
    style,
  ) {
    RichText(
      modifier = modifier,
    ) {
      Markdown(
        content = text,
      )
    }
  }
}
