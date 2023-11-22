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
 * Does so by deciding on a preferred height of [PreferredImageHeight] dp, and a max size of the max possible size
 * given to it from the parent, and a minimum width of that width, multiplied by [MinimumWidthTakenPercentage].
 *
 * Shows a placeholder in case the image size is not yet decided.
 * Then
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

        val potentialWidth = preferredHeight * widthToHeightRatio
        val suggestedSize = when {
          potentialWidth > maxPreferredWidth -> {
            DpSize(maxPreferredWidth, maxPreferredWidth / widthToHeightRatio)
          }

          potentialWidth < minPreferredWidth -> {
            DpSize(minPreferredWidth, minPreferredWidth / widthToHeightRatio)
          }

          else -> {
            DpSize(potentialWidth, preferredHeight)
          }
        }

        val minHeight = MinimumImageHeight.dp
        val finalSize = if (suggestedSize.height < minHeight) {
          suggestedSize.copy(height = minHeight, width = minHeight * widthToHeightRatio)
        } else {
          suggestedSize
        }

        val placeable = measurable.measure(
          Constraints.fixed(finalSize.width.roundToPx(), finalSize.height.roundToPx()),
        )
        layout(placeable.width, placeable.height) {
          placeable.placeRelative(0, 0)
        }
      }
    }
  }
}

private const val PreferredImageHeight = 300
private const val MinimumImageHeight = 100
private const val MinimumWidthTakenPercentage = 0.5f
