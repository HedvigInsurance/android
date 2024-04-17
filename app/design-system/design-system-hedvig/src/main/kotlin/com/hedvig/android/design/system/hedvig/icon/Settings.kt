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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Settings: ImageVector
  get() {
    val current = _settings
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Settings",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M14.25 2.87 a2.82 2.82 0 0 0 -4.5 0 l-.52 .7 A1.3 1.3 0 0 1 8 4.06 L7.13 3.95 a2.82 2.82 0 0 0 -3.18 3.18 L4.07 8 a1.3 1.3 0 0 1 -.5 1.23 l-.7 .52 a2.82 2.82 0 0 0 0 4.5 l.7 .52 A1.3 1.3 0 0 1 4.06 16 l-.12 .87 a2.82 2.82 0 0 0 3.18 3.18 L8 19.93 a1.3 1.3 0 0 1 1.23 .5 l.52 .7 a2.82 2.82 0 0 0 4.5 0 l.52 -.7 a1.3 1.3 0 0 1 1.23 -.5 l.87 .12 a2.82 2.82 0 0 0 3.18 -3.18 L19.93 16 a1.3 1.3 0 0 1 .5 -1.23 l.7 -.52 a2.82 2.82 0 0 0 0 -4.5 l-.7 -.52 A1.3 1.3 0 0 1 19.94 8 l.12 -.87 a2.82 2.82 0 0 0 -3.18 -3.18 L16 4.07 a1.3 1.3 0 0 1 -1.23 -.5z m-3.3 .9 a1.32 1.32 0 0 1 2.1 0 l.53 .7 a2.8 2.8 0 0 0 2.63 1.09 l.87 -.12 a1.32 1.32 0 0 1 1.48 1.48 L18.44 7.8 a2.8 2.8 0 0 0 1.09 2.63 l.7 .53 a1.32 1.32 0 0 1 0 2.1 l-.7 .53 a2.8 2.8 0 0 0 -1.09 2.63 l.12 .87 a1.32 1.32 0 0 1 -1.48 1.48 l-.87 -.12 a2.8 2.8 0 0 0 -2.63 1.09 l-.53 .7 a1.32 1.32 0 0 1 -2.1 0 l-.53 -.7 a2.8 2.8 0 0 0 -2.63 -1.09 l-.87 .12 a1.32 1.32 0 0 1 -1.48 -1.48 l.12 -.87 a2.8 2.8 0 0 0 -1.09 -2.63 l-.7 -.53 a1.32 1.32 0 0 1 0 -2.1 l.7 -.53 A2.8 2.8 0 0 0 5.56 7.8 L5.44 6.92 a1.32 1.32 0 0 1 1.48 -1.48 L7.8 5.56 a2.8 2.8 0 0 0 2.63 -1.09z M14 12 a2 2 0 1 1 -4 0 2 2 0 0 1 4 0 m1.5 0 a3.5 3.5 0 1 1 -7 0 3.5 3.5 0 0 1 7 0
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 14.25 2.87
        moveTo(x = 14.25f, y = 2.87f)
        // a 2.82 2.82 0 0 0 -4.5 0
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -4.5f,
          dy1 = 0.0f,
        )
        // l -0.52 0.7
        lineToRelative(dx = -0.52f, dy = 0.7f)
        // A 1.3 1.3 0 0 1 8 4.06
        arcTo(
          horizontalEllipseRadius = 1.3f,
          verticalEllipseRadius = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 8.0f,
          y1 = 4.06f,
        )
        // L 7.13 3.95
        lineTo(x = 7.13f, y = 3.95f)
        // a 2.82 2.82 0 0 0 -3.18 3.18
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -3.18f,
          dy1 = 3.18f,
        )
        // L 4.07 8
        lineTo(x = 4.07f, y = 8.0f)
        // a 1.3 1.3 0 0 1 -0.5 1.23
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.5f,
          dy1 = 1.23f,
        )
        // l -0.7 0.52
        lineToRelative(dx = -0.7f, dy = 0.52f)
        // a 2.82 2.82 0 0 0 0 4.5
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 4.5f,
        )
        // l 0.7 0.52
        lineToRelative(dx = 0.7f, dy = 0.52f)
        // A 1.3 1.3 0 0 1 4.06 16
        arcTo(
          horizontalEllipseRadius = 1.3f,
          verticalEllipseRadius = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 4.06f,
          y1 = 16.0f,
        )
        // l -0.12 0.87
        lineToRelative(dx = -0.12f, dy = 0.87f)
        // a 2.82 2.82 0 0 0 3.18 3.18
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 3.18f,
          dy1 = 3.18f,
        )
        // L 8 19.93
        lineTo(x = 8.0f, y = 19.93f)
        // a 1.3 1.3 0 0 1 1.23 0.5
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.23f,
          dy1 = 0.5f,
        )
        // l 0.52 0.7
        lineToRelative(dx = 0.52f, dy = 0.7f)
        // a 2.82 2.82 0 0 0 4.5 0
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 4.5f,
          dy1 = 0.0f,
        )
        // l 0.52 -0.7
        lineToRelative(dx = 0.52f, dy = -0.7f)
        // a 1.3 1.3 0 0 1 1.23 -0.5
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.23f,
          dy1 = -0.5f,
        )
        // l 0.87 0.12
        lineToRelative(dx = 0.87f, dy = 0.12f)
        // a 2.82 2.82 0 0 0 3.18 -3.18
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 3.18f,
          dy1 = -3.18f,
        )
        // L 19.93 16
        lineTo(x = 19.93f, y = 16.0f)
        // a 1.3 1.3 0 0 1 0.5 -1.23
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.5f,
          dy1 = -1.23f,
        )
        // l 0.7 -0.52
        lineToRelative(dx = 0.7f, dy = -0.52f)
        // a 2.82 2.82 0 0 0 0 -4.5
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -4.5f,
        )
        // l -0.7 -0.52
        lineToRelative(dx = -0.7f, dy = -0.52f)
        // A 1.3 1.3 0 0 1 19.94 8
        arcTo(
          horizontalEllipseRadius = 1.3f,
          verticalEllipseRadius = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.94f,
          y1 = 8.0f,
        )
        // l 0.12 -0.87
        lineToRelative(dx = 0.12f, dy = -0.87f)
        // a 2.82 2.82 0 0 0 -3.18 -3.18
        arcToRelative(
          a = 2.82f,
          b = 2.82f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -3.18f,
          dy1 = -3.18f,
        )
        // L 16 4.07
        lineTo(x = 16.0f, y = 4.07f)
        // a 1.3 1.3 0 0 1 -1.23 -0.5z
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.23f,
          dy1 = -0.5f,
        )
        close()
        // m -3.3 0.9
        moveToRelative(dx = -3.3f, dy = 0.9f)
        // a 1.32 1.32 0 0 1 2.1 0
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.1f,
          dy1 = 0.0f,
        )
        // l 0.53 0.7
        lineToRelative(dx = 0.53f, dy = 0.7f)
        // a 2.8 2.8 0 0 0 2.63 1.09
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.63f,
          dy1 = 1.09f,
        )
        // l 0.87 -0.12
        lineToRelative(dx = 0.87f, dy = -0.12f)
        // a 1.32 1.32 0 0 1 1.48 1.48
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.48f,
          dy1 = 1.48f,
        )
        // L 18.44 7.8
        lineTo(x = 18.44f, y = 7.8f)
        // a 2.8 2.8 0 0 0 1.09 2.63
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.09f,
          dy1 = 2.63f,
        )
        // l 0.7 0.53
        lineToRelative(dx = 0.7f, dy = 0.53f)
        // a 1.32 1.32 0 0 1 0 2.1
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 2.1f,
        )
        // l -0.7 0.53
        lineToRelative(dx = -0.7f, dy = 0.53f)
        // a 2.8 2.8 0 0 0 -1.09 2.63
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.09f,
          dy1 = 2.63f,
        )
        // l 0.12 0.87
        lineToRelative(dx = 0.12f, dy = 0.87f)
        // a 1.32 1.32 0 0 1 -1.48 1.48
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.48f,
          dy1 = 1.48f,
        )
        // l -0.87 -0.12
        lineToRelative(dx = -0.87f, dy = -0.12f)
        // a 2.8 2.8 0 0 0 -2.63 1.09
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.63f,
          dy1 = 1.09f,
        )
        // l -0.53 0.7
        lineToRelative(dx = -0.53f, dy = 0.7f)
        // a 1.32 1.32 0 0 1 -2.1 0
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.1f,
          dy1 = 0.0f,
        )
        // l -0.53 -0.7
        lineToRelative(dx = -0.53f, dy = -0.7f)
        // a 2.8 2.8 0 0 0 -2.63 -1.09
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.63f,
          dy1 = -1.09f,
        )
        // l -0.87 0.12
        lineToRelative(dx = -0.87f, dy = 0.12f)
        // a 1.32 1.32 0 0 1 -1.48 -1.48
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.48f,
          dy1 = -1.48f,
        )
        // l 0.12 -0.87
        lineToRelative(dx = 0.12f, dy = -0.87f)
        // a 2.8 2.8 0 0 0 -1.09 -2.63
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.09f,
          dy1 = -2.63f,
        )
        // l -0.7 -0.53
        lineToRelative(dx = -0.7f, dy = -0.53f)
        // a 1.32 1.32 0 0 1 0 -2.1
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -2.1f,
        )
        // l 0.7 -0.53
        lineToRelative(dx = 0.7f, dy = -0.53f)
        // A 2.8 2.8 0 0 0 5.56 7.8
        arcTo(
          horizontalEllipseRadius = 2.8f,
          verticalEllipseRadius = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 5.56f,
          y1 = 7.8f,
        )
        // L 5.44 6.92
        lineTo(x = 5.44f, y = 6.92f)
        // a 1.32 1.32 0 0 1 1.48 -1.48
        arcToRelative(
          a = 1.32f,
          b = 1.32f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.48f,
          dy1 = -1.48f,
        )
        // L 7.8 5.56
        lineTo(x = 7.8f, y = 5.56f)
        // a 2.8 2.8 0 0 0 2.63 -1.09z
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.63f,
          dy1 = -1.09f,
        )
        close()
        // M 14 12
        moveTo(x = 14.0f, y = 12.0f)
        // a 2 2 0 1 1 -4 0
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -4.0f,
          dy1 = 0.0f,
        )
        // a 2 2 0 0 1 4 0
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 4.0f,
          dy1 = 0.0f,
        )
        // m 1.5 0
        moveToRelative(dx = 1.5f, dy = 0.0f)
        // a 3.5 3.5 0 1 1 -7 0
        arcToRelative(
          a = 3.5f,
          b = 3.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -7.0f,
          dy1 = 0.0f,
        )
        // a 3.5 3.5 0 0 1 7 0
        arcToRelative(
          a = 3.5f,
          b = 3.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 7.0f,
          dy1 = 0.0f,
        )
      }
    }.build().also { _settings = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.Settings,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _settings: ImageVector? = null
