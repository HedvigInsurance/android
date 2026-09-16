package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface

/**
 * A pill carrying an answer that has already been given. Not a control: its text is selectable so it can be
 * copied, and it is left out of the traversal that reaches the controls.
 */
@Composable
internal fun RoundCornersPill(
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colors = pillColors(isSelected)
  Surface(
    modifier = modifier.clip(HedvigTheme.shapes.cornerXXLarge).defaultMinSize(minWidth = 60.dp),
    shape = HedvigTheme.shapes.cornerXXLarge,
    color = colors.surface,
  ) {
    PillContent(colors.content) {
      SelectionContainer { content() }
    }
  }
}

/**
 * A pill the member acts on.
 *
 * Built on the clickable [Surface] rather than a plain one with a click bolted on. The plain one opts out of
 * merging its descendants and marks itself a traversal group, which is what a bubble wants and what a control
 * does not: it is the difference between a thing to read past and a thing to act on. The clickable surface
 * carries the label, the click and the [role] as one node, the same way every other button here does.
 */
@Composable
internal fun RoundCornersPill(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  role: Role? = null,
  content: @Composable () -> Unit,
) {
  val colors = pillColors(isSelected)
  Surface(
    onClick = onClick,
    modifier = modifier.clip(HedvigTheme.shapes.cornerXXLarge).defaultMinSize(minWidth = 60.dp),
    shape = HedvigTheme.shapes.cornerXXLarge,
    color = colors.surface,
    role = role,
  ) {
    PillContent(colors.content) { content() }
  }
}

private data class PillColors(val surface: Color, val content: Color)

@Composable
private fun pillColors(isSelected: Boolean): PillColors {
  val surface by animateColorAsState(
    if (isSelected) {
      HedvigTheme.colorScheme.signalGreenFill.compositeOver(HedvigTheme.colorScheme.backgroundPrimary)
    } else {
      HedvigTheme.colorScheme.buttonSecondaryResting
    },
  )
  val content by animateColorAsState(
    if (isSelected) {
      HedvigTheme.colorScheme.signalGreenText.compositeOver(surface)
    } else {
      HedvigTheme.colorScheme.textPrimary
    },
  )
  return PillColors(surface, content)
}

@Composable
private fun PillContent(contentColor: Color, content: @Composable () -> Unit) {
  Column(
    Modifier.padding(
      top = 7.dp,
      start = 14.dp,
      end = 14.dp,
      bottom = 9.dp,
    ),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    ProvideTextStyle(LocalTextStyle.current.copy(color = contentColor)) {
      content()
    }
  }
}
