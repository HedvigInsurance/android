package com.hedvig.app.util.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.designsystem.theme.HedvigTheme
import kotlin.math.max

/**
 * When two texts need to be laid out horizontally in a row, they can't know how much space they need to take out, which
 * more often than not results in the starting text taking up all the width it needs, squeezing the end text. This
 * layout makes sure to measure their max intrinsic width and give as much space as possible to each text, without
 * squeezing the other one. If both of them were to need more than half of the space, or less than half of the space,
 * they're simply given half of the width each.
 */
@Composable
fun HorizontalTextsWithMaximumSpaceTaken(
  startText: @Composable () -> Unit,
  endText: @Composable (textAlign: TextAlign) -> Unit,
  modifier: Modifier = Modifier,
  spaceBetween: Dp = 0.dp,
) {
  Layout(
    content = {
      startText()
      endText(textAlign = TextAlign.End)
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val first = measurables[0]
    val second = measurables[1]
    val firstWidth = first.maxIntrinsicWidth(constraints.maxHeight)
    val secondWidth = second.maxIntrinsicWidth(constraints.maxHeight)

    val totalWidth = constraints.maxWidth
    val halfWidth = totalWidth / 2

    val centerSpace = spaceBetween.roundToPx()
    val halfCenterSpace = centerSpace / 2

    val halfWidthMinusSpace = halfWidth - halfCenterSpace

    val bothTakeLessThanHalfSpace = firstWidth <= halfWidthMinusSpace && secondWidth <= halfWidthMinusSpace
    val bothTakeMoreThanHalfSpace = firstWidth > halfWidthMinusSpace && secondWidth > halfWidthMinusSpace
    val textsShouldShareEqualSpace = bothTakeLessThanHalfSpace || bothTakeMoreThanHalfSpace

    val firstConstraints: Constraints
    val secondConstraints: Constraints
    if (textsShouldShareEqualSpace) {
      val halfWidthMinusSpaceConstraints = constraints.copy(
        minWidth = halfWidthMinusSpace,
        maxWidth = halfWidthMinusSpace,
      )
      firstConstraints = halfWidthMinusSpaceConstraints
      secondConstraints = halfWidthMinusSpaceConstraints
    } else if (firstWidth > halfWidthMinusSpace) {
      firstConstraints = constraints.copy(
        minWidth = totalWidth - secondWidth - halfCenterSpace,
        maxWidth = totalWidth - secondWidth - halfCenterSpace,
      )
      secondConstraints = constraints.copy(
        minWidth = secondWidth,
        maxWidth = secondWidth,
      )
    } else {
      firstConstraints = constraints.copy(
        minWidth = firstWidth,
        maxWidth = firstWidth,
      )
      secondConstraints = constraints.copy(
        minWidth = totalWidth - firstWidth - halfCenterSpace,
        maxWidth = totalWidth - firstWidth - halfCenterSpace,
      )
    }
    val firstPlaceable = first.measure(firstConstraints)
    val secondPlaceable = second.measure(secondConstraints)
    layout(constraints.maxWidth, max(firstPlaceable.height, secondPlaceable.height)) {
      firstPlaceable.placeRelative(0, 0)
      secondPlaceable.placeRelative(constraints.maxWidth - secondPlaceable.width, 0)
    }
  }
}

@Preview
@Composable
fun SmallTextsPreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start") },
      endText = { Text(text = "End", textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
    )
  }
}

@Preview
@Composable
fun BigTextsPreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start".repeat(10)) },
      endText = { Text(text = "End".repeat(10), textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
    )
  }
}

@Preview
@Composable
fun BigTextsWithSpaceTextPreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start".repeat(10)) },
      endText = { Text(text = "End".repeat(10), textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
      spaceBetween = 30.dp,
    )
  }
}

@Preview
@Composable
fun BigStartTextPreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start".repeat(10)) },
      endText = { Text(text = "End", textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
    )
  }
}

@Preview
@Composable
fun BigEndTextPreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start") },
      endText = { Text(text = "End".repeat(10), textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
    )
  }
}

@Preview
@Composable
fun BigEndTextWithSpacePreview() {
  HedvigTheme {
    HorizontalTextsWithMaximumSpaceTaken(
      startText = { Text(text = "Start") },
      endText = { Text(text = "End".repeat(10), textAlign = it) },
      modifier = Modifier.size(width = 250.dp, height = 100.dp),
      spaceBetween = 32.dp,
    )
  }
}
