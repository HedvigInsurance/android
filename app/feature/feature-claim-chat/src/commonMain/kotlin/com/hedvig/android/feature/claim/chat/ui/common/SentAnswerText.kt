package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.TextLayoutResult
import com.hedvig.android.design.system.hedvig.HedvigText
import kotlin.math.ceil
import kotlin.math.floor

/**
 * The text of an answer the member has sent, laid out as wide as its widest line.
 *
 * A text that wraps takes the whole width it is offered, however short its lines come out, so the bubble around
 * it would span the row and the slack would show as empty bubble beside the shorter lines. This reports the span
 * of its lines as its width instead, and shifts the text so that span starts at its own left edge. The lines are
 * read off the layout the text produces while it is measured, so nothing is laid out twice.
 */
@Composable
internal fun SentAnswerText(text: String, modifier: Modifier = Modifier) {
  val lines = remember { Ref<TextLayoutResult>() }
  Layout(
    content = { HedvigText(text, onTextLayout = { lines.value = it }) },
    modifier = modifier,
  ) { measurables, constraints ->
    val placeable = measurables.single().measure(constraints)
    val result = lines.value
    if (result == null || result.lineCount <= 1) {
      layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    } else {
      var left = Float.MAX_VALUE
      var right = 0f
      for (line in 0 until result.lineCount) {
        left = minOf(left, result.getLineLeft(line))
        right = maxOf(right, result.getLineRight(line))
      }
      val start = floor(left).toInt()
      val width = (ceil(right).toInt() - start).coerceIn(constraints.minWidth, placeable.width)
      layout(width, placeable.height) { placeable.place(-start, 0) }
    }
  }
}
