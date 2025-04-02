package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
  spaceBetween: Dp,
  modifier: Modifier = Modifier,
) {
  Layout(
    content = {
      startSlot()
      endSlot()
    },
    modifier = modifier.semantics(mergeDescendants = true) {},
  ) { measurables, constraints ->
    val first = measurables.getOrNull(0)
    val second = measurables.getOrNull(1)
    val firstWidth = first?.maxIntrinsicWidth(constraints.maxHeight) ?: 0
    val secondWidth = second?.maxIntrinsicWidth(constraints.maxHeight) ?: 0

    val totalWidth = constraints.maxWidth
    val halfWidth = totalWidth / 2

    val centerSpace = spaceBetween.roundToPx().takeIf { firstWidth != 0 && secondWidth != 0 } ?: 0
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
        minWidth = totalWidth - secondWidth - centerSpace,
        maxWidth = totalWidth - secondWidth - centerSpace,
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
        minWidth = totalWidth - firstWidth - centerSpace,
        maxWidth = totalWidth - firstWidth - centerSpace,
      )
    }
    val maxCommonHeight = maxOf(
      first?.minIntrinsicHeight(firstConstraints.maxWidth) ?: 0,
      second?.minIntrinsicHeight(secondConstraints.maxWidth) ?: 0,
    )
    val firstPlaceable = first?.measure(
      firstConstraints.copy(
        minHeight = maxCommonHeight,
        maxHeight = maxCommonHeight,
      ),
    )
    val secondPlaceable = second?.measure(
      secondConstraints.copy(
        minHeight = maxCommonHeight,
        maxHeight = maxCommonHeight,
      ),
    )
    val layoutHeight = max(firstPlaceable?.height ?: 0, secondPlaceable?.height ?: 0)
    layout(constraints.maxWidth, layoutHeight) {
      firstPlaceable?.placeRelative(0, 0)
      secondPlaceable?.placeRelative(constraints.maxWidth - secondPlaceable.width, 0)
    }
  }
}
