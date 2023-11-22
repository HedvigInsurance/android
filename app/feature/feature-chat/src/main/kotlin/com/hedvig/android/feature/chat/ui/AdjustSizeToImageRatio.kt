package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.fade
import com.hedvig.android.placeholder.placeholder

/**
 * Adjusts the composable's size to match the image's aspect ratio.
 * Does so by starting wiht a preferred height of [PreferredImageHeight] dp, and a max width of the max possible width
 * given to it from the parent, and a minimum width of that width, multiplied by [MinimumWidthTakenPercentage].
 *
 * Then it makes the image take as much space while respecting those bounds given to it.
 *
 * Shows a placeholder in case the image size is not yet decided.
 */
internal fun Modifier.adjustSizeToImageRatioOrShowPlaceholder(getImageSize: () -> IntSize?): Modifier = this.composed {
  when {
    getImageSize() == null -> {
      Modifier
        .size(100.dp)
        .placeholder(visible = true, highlight = PlaceholderHighlight.fade())
    }

    else -> {
      Modifier.layout { measurable, constraints ->
        val imageSize = getImageSize()
        val widthToHeightRatio = imageSize?.width?.toFloat()?.div(imageSize.height) ?: 1f

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
  }
}

private const val MaximumImageHeight = 250
private const val PreferredImageHeight = 180
private const val MinimumImageHeight = 100
private const val MinimumWidthTakenPercentage = 0.5f
