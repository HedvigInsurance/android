package com.hedvig.android.design.system.hedvig.a11y

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Composable
fun DoubleTitleHeading(
  title: String,
  description: String?,
  modifier: Modifier = Modifier,
  baseStyle: TextStyle = HedvigTheme.typography.headlineMedium,
) {
  Column(
    modifier = modifier.semantics(mergeDescendants = true) {
      heading()
    },
  ) {
    HedvigText(
      text = title,
      style = baseStyle.copy(lineBreak = LineBreak.Heading),
    )
    if (description != null) {
      HedvigText(
        text = description,
        style = baseStyle.copy(
          lineBreak = LineBreak.Heading,
          color = HedvigTheme.colorScheme.textSecondary,
        ),
      )
    }
  }
}
