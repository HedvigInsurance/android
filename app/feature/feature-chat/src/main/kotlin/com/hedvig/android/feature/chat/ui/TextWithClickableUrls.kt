package com.hedvig.android.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.HedvigMarkdownText
import com.hedvig.android.design.system.hedvig.ProvideTextStyle

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun TextWithClickableUrls(text: String, modifier: Modifier = Modifier, style: TextStyle = TextStyle.Default) {
  ProvideTextStyle(
    style,
  ) {
    HedvigMarkdownText(
      content = text,
      modifier = modifier,
    )
  }
}
