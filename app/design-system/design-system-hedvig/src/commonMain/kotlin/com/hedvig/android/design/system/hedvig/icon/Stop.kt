package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Stop: ImageVector
  get() {
    val current = _stop
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Stop",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // Rounded square: 12dp x 12dp with 2dp corner radius, centered in 24x24 viewport
      // Top-left corner at (6, 6), bottom-right at (18, 18), corner radius = 2
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 8 6 - Start at top edge, offset by corner radius
        moveTo(x = 8.0f, y = 6.0f)
        // H 16 - Draw top edge to top-right corner
        horizontalLineTo(x = 16.0f)
        // Quadratic curve for top-right corner (control point at corner, end at right edge start)
        curveTo(
          x1 = 18.0f,
          y1 = 6.0f,
          x2 = 18.0f,
          y2 = 6.0f,
          x3 = 18.0f,
          y3 = 8.0f,
        )
        // V 16 - Draw right edge to bottom-right corner
        verticalLineTo(y = 16.0f)
        // Quadratic curve for bottom-right corner
        curveTo(
          x1 = 18.0f,
          y1 = 18.0f,
          x2 = 18.0f,
          y2 = 18.0f,
          x3 = 16.0f,
          y3 = 18.0f,
        )
        // H 8 - Draw bottom edge to bottom-left corner
        horizontalLineTo(x = 8.0f)
        // Quadratic curve for bottom-left corner
        curveTo(
          x1 = 6.0f,
          y1 = 18.0f,
          x2 = 6.0f,
          y2 = 18.0f,
          x3 = 6.0f,
          y3 = 16.0f,
        )
        // V 8 - Draw left edge to top-left corner
        verticalLineTo(y = 8.0f)
        // Quadratic curve for top-left corner back to start
        curveTo(
          x1 = 6.0f,
          y1 = 6.0f,
          x2 = 6.0f,
          y2 = 6.0f,
          x3 = 8.0f,
          y3 = 6.0f,
        )
        close()
      }
    }.build().also { _stop = it }
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Stop,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _stop: ImageVector? = null
