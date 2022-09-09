package com.hedvig.app.feature.embark.passages.selectaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * A grid which:
 * - Places items in a 2-wide grid, with the last item in the center if the number of items are odd
 * - Takes on the entire width given by its constraints, and gives each child half of it as a fixed width constraint
 * - Calculates the maximum intrinsic height of each child and measures them all with the maximum one provided to them
 *   as a fixed height constraint
 * ```
 * +------------------------+    +------------------------+
 * |+----------++----------+|    |+----------++----------+|
 * |+ item #1  ++ item #2  +|    |+ item #1  ++ item #2  +|
 * |+----------++----------+|    |+----------++----------+|
 * |+     +----------+     +|    |+----------++----------+|
 * |+     + item #3  +     +|    |+ item #3  ++ item #4  +|
 * |+     +----------+     +|    |+----------++----------+|
 * +------------------------+    +------------------------+
 * ```
 */
@Composable
fun SelectActionGrid(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  insideGridSpace: InsideGridSpace = InsideGridSpace(0.dp),
  content: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier
      .padding(contentPadding)
      .testTag("selectActionGrid"),
    content = content,
  ) { measurables, constraints ->
    val horizontalSpacingInPx = insideGridSpace.horizontal.roundToPx()
    val width = (constraints.maxWidth / 2) - (horizontalSpacingInPx / 2)
    val height = measurables.maxOf { it.maxIntrinsicHeight(width) }
    val itemConstraint = constraints.copy(
      minHeight = height,
      maxHeight = height,
      maxWidth = width,
      minWidth = width,
    )
    val placeables = measurables.map { measurable ->
      measurable.measure(itemConstraint)
    }

    var yPosition = 0
    val placeableWithCoordinatesList: List<PlaceableWithCoordinates> = buildList {
      placeables.chunked(2) { placeables ->
        if (placeables.size == 1) {
          val (placeable) = placeables
          add(
            placeable.withCoordinates(
              (constraints.maxWidth / 2) - (placeable.width / 2),
              yPosition,
            ),
          )
        } else if (placeables.size == 2) {
          val (placeableStart, placeableEnd) = placeables
          add(
            placeableStart.withCoordinates(
              x = 0,
              y = yPosition,
            ),
          )
          add(
            placeableEnd.withCoordinates(
              x = (constraints.maxWidth / 2) + (horizontalSpacingInPx / 2),
              y = yPosition,
            ),
          )
        }
        yPosition += placeables.maxOf(Placeable::height) + insideGridSpace.vertical.roundToPx()
      }
    }
    yPosition -= insideGridSpace.vertical.roundToPx()

    layout(constraints.maxWidth, yPosition.coerceAtMost(constraints.maxHeight)) {
      placeableWithCoordinatesList.forEach { (placeable, x, y) ->
        placeable.placeRelative(x, y)
      }
    }
  }
}

private class PlaceableWithCoordinates(
  private val placeable: Placeable,
  private val x: Int,
  private val y: Int,
) {
  operator fun component1() = placeable
  operator fun component2() = x
  operator fun component3() = y
}

private fun Placeable.withCoordinates(
  x: Int,
  y: Int,
): PlaceableWithCoordinates = PlaceableWithCoordinates(this, x, y)

@Preview
@Composable
fun SelectActionGridPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      SelectActionGrid {
        repeat(3) { index ->
          val normalIndex = index + 1
          Box(
            modifier = Modifier
              .size((50 * normalIndex).dp)
              .background(Color(red = 255 / normalIndex, green = 128, blue = 128)),
          )
        }
      }
    }
  }
}
