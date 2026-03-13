package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun RoundCornersPill(
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  onClick: (() -> Unit)?,
  content: @Composable () -> Unit,
) {
  val surfaceColor by animateColorAsState(
    if (isSelected) {
      HedvigTheme.colorScheme.signalGreenFill.compositeOver(HedvigTheme.colorScheme.backgroundPrimary)
    } else {
      HedvigTheme.colorScheme.buttonSecondaryResting
    },
  )
  val contentColor by animateColorAsState(
    if (isSelected) {
      HedvigTheme.colorScheme.signalGreenText.compositeOver(surfaceColor)
    } else {
      HedvigTheme.colorScheme.textPrimary
    },
  )
  Surface(
    modifier
      .clip(HedvigTheme.shapes.cornerXXLarge)
      .then(
        if (onClick != null) {
          Modifier.clickable(
            onClick = onClick,
          )
        } else {
          Modifier
        },
      ),
    shape = HedvigTheme.shapes.cornerXXLarge,
    color = surfaceColor,
  ) {
    Column(
      Modifier.Companion.padding(
        top = 7.dp,
        start = 14.dp,
        end = 14.dp,
        bottom = 9.dp,
      ),
    ) {
      ProvideTextStyle(LocalTextStyle.current.copy(color = contentColor)) {
        if (onClick != null) {
          content()
        } else {
          SelectionContainer {
            content()
          }
        }
      }
    }
  }
}
