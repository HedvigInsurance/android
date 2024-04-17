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
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Cart: ImageVector
  get() {
    val current = _cart
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Cart",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      group {
        // M20.04 3.85 c-1.57 -1.52 -4.1 -2.35 -8.05 -2.35 -3.98 0 -6.5 .8 -8.04 2.3 S1.5 7.8 1.5 12 s.9 6.7 2.45 8.2 4.06 2.3 8.04 2.3 c3.95 0 6.48 -.83 8.05 -2.35 1.55 -1.52 2.46 -4.03 2.46 -8.15 s-.9 -6.63 -2.46 -8.15 M11.99 0 C20.23 0 24 3.4 24 12 s-3.77 12 -12.01 12 S0 20.74 0 12 3.74 0 11.99 0
        path(
          fill = SolidColor(Color(0xFF121212)),
          pathFillType = PathFillType.EvenOdd,
        ) {
          // M 20.04 3.85
          moveTo(x = 20.04f, y = 3.85f)
          // c -1.57 -1.52 -4.1 -2.35 -8.05 -2.35
          curveToRelative(
            dx1 = -1.57f,
            dy1 = -1.52f,
            dx2 = -4.1f,
            dy2 = -2.35f,
            dx3 = -8.05f,
            dy3 = -2.35f,
          )
          // c -3.98 0 -6.5 0.8 -8.04 2.3
          curveToRelative(
            dx1 = -3.98f,
            dy1 = 0.0f,
            dx2 = -6.5f,
            dy2 = 0.8f,
            dx3 = -8.04f,
            dy3 = 2.3f,
          )
          // S 1.5 7.8 1.5 12
          reflectiveCurveTo(
            x1 = 1.5f,
            y1 = 7.8f,
            x2 = 1.5f,
            y2 = 12.0f,
          )
          // s 0.9 6.7 2.45 8.2
          reflectiveCurveToRelative(
            dx1 = 0.9f,
            dy1 = 6.7f,
            dx2 = 2.45f,
            dy2 = 8.2f,
          )
          // s 4.06 2.3 8.04 2.3
          reflectiveCurveToRelative(
            dx1 = 4.06f,
            dy1 = 2.3f,
            dx2 = 8.04f,
            dy2 = 2.3f,
          )
          // c 3.95 0 6.48 -0.83 8.05 -2.35
          curveToRelative(
            dx1 = 3.95f,
            dy1 = 0.0f,
            dx2 = 6.48f,
            dy2 = -0.83f,
            dx3 = 8.05f,
            dy3 = -2.35f,
          )
          // c 1.55 -1.52 2.46 -4.03 2.46 -8.15
          curveToRelative(
            dx1 = 1.55f,
            dy1 = -1.52f,
            dx2 = 2.46f,
            dy2 = -4.03f,
            dx3 = 2.46f,
            dy3 = -8.15f,
          )
          // s -0.9 -6.63 -2.46 -8.15
          reflectiveCurveToRelative(
            dx1 = -0.9f,
            dy1 = -6.63f,
            dx2 = -2.46f,
            dy2 = -8.15f,
          )
          // M 11.99 0
          moveTo(x = 11.99f, y = 0.0f)
          // C 20.23 0 24 3.4 24 12
          curveTo(
            x1 = 20.23f,
            y1 = 0.0f,
            x2 = 24.0f,
            y2 = 3.4f,
            x3 = 24.0f,
            y3 = 12.0f,
          )
          // s -3.77 12 -12.01 12
          reflectiveCurveToRelative(
            dx1 = -3.77f,
            dy1 = 12.0f,
            dx2 = -12.01f,
            dy2 = 12.0f,
          )
          // S 0 20.74 0 12
          reflectiveCurveTo(
            x1 = 0.0f,
            y1 = 20.74f,
            x2 = 0.0f,
            y2 = 12.0f,
          )
          // S 3.74 0 11.99 0
          reflectiveCurveTo(
            x1 = 3.74f,
            y1 = 0.0f,
            x2 = 11.99f,
            y2 = 0.0f,
          )
        }
        // M12 17.14 c-2.37 0 -3.6 -1.88 -3.6 -5.04 S9.63 7.06 12 7.06 s3.6 1.88 3.6 5.04 -1.23 5.04 -3.6 5.04 M9.73 12.1 q0 4.02 2.27 4.03 2.27 -.01 2.27 -4.03 T12 8.07 Q9.73 8.08 9.73 12.1
        path(
          fill = SolidColor(Color(0xFF121212)),
          pathFillType = PathFillType.EvenOdd,
        ) {
          // M 12 17.14
          moveTo(x = 12.0f, y = 17.14f)
          // c -2.37 0 -3.6 -1.88 -3.6 -5.04
          curveToRelative(
            dx1 = -2.37f,
            dy1 = 0.0f,
            dx2 = -3.6f,
            dy2 = -1.88f,
            dx3 = -3.6f,
            dy3 = -5.04f,
          )
          // S 9.63 7.06 12 7.06
          reflectiveCurveTo(
            x1 = 9.63f,
            y1 = 7.06f,
            x2 = 12.0f,
            y2 = 7.06f,
          )
          // s 3.6 1.88 3.6 5.04
          reflectiveCurveToRelative(
            dx1 = 3.6f,
            dy1 = 1.88f,
            dx2 = 3.6f,
            dy2 = 5.04f,
          )
          // s -1.23 5.04 -3.6 5.04
          reflectiveCurveToRelative(
            dx1 = -1.23f,
            dy1 = 5.04f,
            dx2 = -3.6f,
            dy2 = 5.04f,
          )
          // M 9.73 12.1
          moveTo(x = 9.73f, y = 12.1f)
          // q 0 4.02 2.27 4.03
          quadToRelative(
            dx1 = 0.0f,
            dy1 = 4.02f,
            dx2 = 2.27f,
            dy2 = 4.03f,
          )
          // q 2.27 -0.01 2.27 -4.03
          quadToRelative(
            dx1 = 2.27f,
            dy1 = -0.01f,
            dx2 = 2.27f,
            dy2 = -4.03f,
          )
          // T 12 8.07
          reflectiveQuadTo(
            x1 = 12.0f,
            y1 = 8.07f,
          )
          // Q 9.73 8.08 9.73 12.1
          quadTo(
            x1 = 9.73f,
            y1 = 8.08f,
            x2 = 9.73f,
            y2 = 12.1f,
          )
        }
      }
    }.build().also { _cart = it }
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
        imageVector = Cart,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _cart: ImageVector? = null
