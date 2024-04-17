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

val ShieldFilled: ImageVector
  get() {
    val current = _shieldFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ShieldFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.3 21.25 a2.4 2.4 0 0 1 -2.6 0 C8.44 19.83 4 16.47 4 12 V7.08 a3 3 0 0 1 1.95 -2.81 l5 -1.87 a3 3 0 0 1 2.1 0 l5 1.87 A3 3 0 0 1 20 7.07 V12 c0 4.47 -4.44 7.83 -6.7 9.25 m2.38 -10.72 a.75 .75 0 1 0 -1.06 -1.06 l-3.33 3.3 a.25 .25 0 0 1 -.35 0 l-1.26 -1.25 a.75 .75 0 1 0 -1.06 1.06 l1.26 1.25 a1.75 1.75 0 0 0 2.47 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 13.3 21.25
        moveTo(x = 13.3f, y = 21.25f)
        // a 2.4 2.4 0 0 1 -2.6 0
        arcToRelative(
          a = 2.4f,
          b = 2.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.6f,
          dy1 = 0.0f,
        )
        // C 8.44 19.83 4 16.47 4 12
        curveTo(
          x1 = 8.44f,
          y1 = 19.83f,
          x2 = 4.0f,
          y2 = 16.47f,
          x3 = 4.0f,
          y3 = 12.0f,
        )
        // V 7.08
        verticalLineTo(y = 7.08f)
        // a 3 3 0 0 1 1.95 -2.81
        arcToRelative(
          a = 3.0f,
          b = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.95f,
          dy1 = -2.81f,
        )
        // l 5 -1.87
        lineToRelative(dx = 5.0f, dy = -1.87f)
        // a 3 3 0 0 1 2.1 0
        arcToRelative(
          a = 3.0f,
          b = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.1f,
          dy1 = 0.0f,
        )
        // l 5 1.87
        lineToRelative(dx = 5.0f, dy = 1.87f)
        // A 3 3 0 0 1 20 7.07
        arcTo(
          horizontalEllipseRadius = 3.0f,
          verticalEllipseRadius = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 20.0f,
          y1 = 7.07f,
        )
        // V 12
        verticalLineTo(y = 12.0f)
        // c 0 4.47 -4.44 7.83 -6.7 9.25
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 4.47f,
          dx2 = -4.44f,
          dy2 = 7.83f,
          dx3 = -6.7f,
          dy3 = 9.25f,
        )
        // m 2.38 -10.72
        moveToRelative(dx = 2.38f, dy = -10.72f)
        // a 0.75 0.75 0 1 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l -3.33 3.3
        lineToRelative(dx = -3.33f, dy = 3.3f)
        // a 0.25 0.25 0 0 1 -0.35 0
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.35f,
          dy1 = 0.0f,
        )
        // l -1.26 -1.25
        lineToRelative(dx = -1.26f, dy = -1.25f)
        // a 0.75 0.75 0 1 0 -1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        // l 1.26 1.25
        lineToRelative(dx = 1.26f, dy = 1.25f)
        // a 1.75 1.75 0 0 0 2.47 0z
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.47f,
          dy1 = 0.0f,
        )
        close()
      }
    }.build().also { _shieldFilled = it }
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
        imageVector = ShieldFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _shieldFilled: ImageVector? = null
