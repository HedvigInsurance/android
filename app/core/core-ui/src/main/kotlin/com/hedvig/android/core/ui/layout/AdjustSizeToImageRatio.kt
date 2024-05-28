package com.hedvig.android.core.ui.layout

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Adjusts the composable's size to match the image's aspect ratio.
 * Does so by starting wiht a preferred height of [PreferredImageHeight] dp. Then it tries to adjust the width of the
 * image acording to the w/h ratio while keeping that height.
 * If the width then ends up being too little (by multiplying max width by [MinimumWidthTakenPercentage]), it will take
 * the min width as a fact and adjust the height accordingly. If the width ends up being too much, it keeps the max
 * width and adjusts the height to that width instead.
 * With that size, it does the same check and contains the final size to ([MinimumImageHeight]..[MaximumImageHeight]),
 * again adjusting the width to that new height.
 */
fun Modifier.adjustSizeToImageRatio(getImageSize: () -> IntSize?): Modifier = this.then(
  when {
    getImageSize() == null -> {
      Modifier.size(100.dp)
    }

    else -> {
      Modifier.layout { measurable, constraints ->
        val imageSize = getImageSize()
        val widthToHeightRatio =
          imageSize?.width?.toFloat()?.div(imageSize.height).takeIf { it?.isNaN() == false } ?: 1f

        val minPreferredWidth = constraints.maxWidth.toDp() * MinimumWidthTakenPercentage
        val maxPreferredWidth = constraints.maxWidth.toDp()
        val preferredHeight = PreferredImageHeight.dp

        val ratioAdjustedWidth = preferredHeight * widthToHeightRatio
        val widthBoundAdjustedSize = when {
          ratioAdjustedWidth > maxPreferredWidth -> {
            DpSize(width = maxPreferredWidth, height = maxPreferredWidth / widthToHeightRatio)
          }

          ratioAdjustedWidth < minPreferredWidth -> {
            DpSize(width = minPreferredWidth, height = minPreferredWidth / widthToHeightRatio)
          }

          else -> DpSize(ratioAdjustedWidth, preferredHeight)
        }

        val minHeight = MinimumImageHeight.dp
        val maxHeight = MaximumImageHeight.dp
        val widthAndHeightAdjustedSize = when {
          widthBoundAdjustedSize.height < minHeight -> {
            widthBoundAdjustedSize.copy(width = minHeight * widthToHeightRatio, height = minHeight)
          }

          widthBoundAdjustedSize.height > maxHeight -> {
            widthBoundAdjustedSize.copy(width = maxHeight * widthToHeightRatio, height = maxHeight)
          }

          else -> widthBoundAdjustedSize
        }

        val placeable = measurable.measure(
          Constraints.fixed(
            widthAndHeightAdjustedSize.width.roundToPx(),
            widthAndHeightAdjustedSize.height.roundToPx(),
          ),
        )
        layout(placeable.width, placeable.height) {
          placeable.placeRelative(0, 0)
        }
      }
    }
  },
)

private const val MaximumImageHeight = 250
private const val PreferredImageHeight = 180
private const val MinimumImageHeight = 100
private const val MinimumWidthTakenPercentage = 0.5f
