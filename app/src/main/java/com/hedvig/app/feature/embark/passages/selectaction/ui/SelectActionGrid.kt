package com.hedvig.app.feature.embark.passages.selectaction

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.embark.passages.selectaction.ui.InsideGridSpace

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun SelectActionGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    insideGridSpace: InsideGridSpace = InsideGridSpace(0.dp),
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier.padding(contentPadding),
        content = content
    ) { measurables, constraints ->
        val horizontalSpacingInPx = insideGridSpace.horizontal.roundToPx()
        val placeables = measurables.map { measurable ->
            val width = (constraints.maxWidth / 2) - (horizontalSpacingInPx / 2)
            val halfWidthConstraint = constraints.copy(
                maxWidth = width,
                minWidth = width
            )
            measurable.measure(halfWidthConstraint)
        }

        var yPosition = 0
        val placeableWithCoordinatesList = buildList<PlaceableWithCoordinates> {
            placeables.chunked(2) { placeables ->
                if (placeables.size == 1) {
                    val (placeable) = placeables
                    add(
                        placeable.withCoordinates(
                            (constraints.maxWidth / 2) - (placeable.width / 2),
                            yPosition
                        )
                    )
                } else if (placeables.size == 2) {
                    val (placeableStart, placeableEnd) = placeables
                    add(
                        placeableStart.withCoordinates(
                            x = 0,
                            y = yPosition
                        )
                    )
                    add(
                        placeableEnd.withCoordinates(
                            x = (constraints.maxWidth / 2) + (horizontalSpacingInPx / 2),
                            y = yPosition
                        )
                    )
                }
                yPosition += placeables.maxOf(Placeable::height) + insideGridSpace.vertical.roundToPx()
            }
        }
        yPosition -= insideGridSpace.vertical.roundToPx()

        layout(constraints.maxWidth, yPosition) {
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
