package com.hedvig.android.core.ui.text

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import kotlin.math.max

/**
 * When two items need to be laid out horizontally in a row, they can't know how much space they need to take out, which
 * more often than not results in the starting item taking up all the width it needs, squeezing the end item. This
 * layout makes sure to measure their max intrinsic width and give as much space as possible to each item, without
 * squeezing the other one. If both of them were to need more than half of the space, or less than half of the space,
 * they're simply given half of the width each.
 */
@Composable
fun HorizontalItemsWithMaximumSpaceTaken(
  startSlot: @Composable () -> Unit,
  endSlot: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  spaceBetween: Dp = 0.dp,
) {
  Layout(
    content = {
      startSlot()
      endSlot()
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val first = measurables.getOrNull(0)
    val second = measurables.getOrNull(1)
    val firstWidth = first?.maxIntrinsicWidth(constraints.maxHeight) ?: 0
    val secondWidth = second?.maxIntrinsicWidth(constraints.maxHeight) ?: 0

    val totalWidth = constraints.maxWidth
    val halfWidth = totalWidth / 2

    val centerSpace = spaceBetween.roundToPx()
    val halfCenterSpace = centerSpace / 2

    val halfWidthMinusSpace = halfWidth - halfCenterSpace

    val bothTakeLessThanHalfSpace =
      firstWidth <= halfWidthMinusSpace && secondWidth <= halfWidthMinusSpace
    val bothTakeMoreThanHalfSpace =
      firstWidth > halfWidthMinusSpace && secondWidth > halfWidthMinusSpace
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
    val firstPlaceable = first?.measure(firstConstraints)
    val secondPlaceable = second?.measure(secondConstraints)
    val layoutHeight = max(firstPlaceable?.height ?: 0, secondPlaceable?.height ?: 0)
    layout(constraints.maxWidth, layoutHeight) {
      firstPlaceable?.placeRelative(0, 0)
      secondPlaceable?.placeRelative(constraints.maxWidth - secondPlaceable.width, 0)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSmallTexts() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start") },
        endSlot = { Text(text = "End", textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBigTexts() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start".repeat(10)) },
        endSlot = { Text(text = "End".repeat(10), textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBigTextsWithSpaceText() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start".repeat(10)) },
        endSlot = { Text(text = "End".repeat(10), textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
        spaceBetween = 30.dp,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBigStartText() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start".repeat(10)) },
        endSlot = { Text(text = "End", textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBigEndText() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start") },
        endSlot = { Text(text = "End".repeat(10), textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBigEndTextWithSpace() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(text = "Start") },
        endSlot = { Text(text = "End".repeat(10), textAlign = TextAlign.End) },
        modifier = Modifier.size(width = 250.dp, height = 100.dp),
        spaceBetween = 32.dp,
      )
    }
  }
}
