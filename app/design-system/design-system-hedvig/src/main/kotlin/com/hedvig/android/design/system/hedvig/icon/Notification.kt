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
val HedvigIcons.Notification: ImageVector
  get() {
    val current = _notification
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Notification",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.67 3.83 c1.14 .14 2.2 .48 2.9 1.3 a6 6 0 0 1 1.27 2.83 l2.12 8.02 c.22 .98 -.53 1.86 -1.48 1.76 H5.63 c-1.06 0 -1.8 -.88 -1.6 -1.76 l2.13 -8.02 a6.5 6.5 0 0 1 1.29 -2.8 c.69 -.84 1.74 -1.19 2.87 -1.32 .23 -.65 .91 -1.09 1.68 -1.09 s1.45 .44 1.67 1.08 m.08 15.89 c0 .88 -.82 1.53 -1.75 1.53 s-1.75 -.65 -1.75 -1.53 c0 -.3 .26 -.47 .5 -.47 h2.5 c.24 0 .5 .18 .5 .47
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 13.67 3.83
        moveTo(x = 13.67f, y = 3.83f)
        // c 1.14 0.14 2.2 0.48 2.9 1.3
        curveToRelative(
          dx1 = 1.14f,
          dy1 = 0.14f,
          dx2 = 2.2f,
          dy2 = 0.48f,
          dx3 = 2.9f,
          dy3 = 1.3f,
        )
        // a 6 6 0 0 1 1.27 2.83
        arcToRelative(
          a = 6.0f,
          b = 6.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.27f,
          dy1 = 2.83f,
        )
        // l 2.12 8.02
        lineToRelative(dx = 2.12f, dy = 8.02f)
        // c 0.22 0.98 -0.53 1.86 -1.48 1.76
        curveToRelative(
          dx1 = 0.22f,
          dy1 = 0.98f,
          dx2 = -0.53f,
          dy2 = 1.86f,
          dx3 = -1.48f,
          dy3 = 1.76f,
        )
        // H 5.63
        horizontalLineTo(x = 5.63f)
        // c -1.06 0 -1.8 -0.88 -1.6 -1.76
        curveToRelative(
          dx1 = -1.06f,
          dy1 = 0.0f,
          dx2 = -1.8f,
          dy2 = -0.88f,
          dx3 = -1.6f,
          dy3 = -1.76f,
        )
        // l 2.13 -8.02
        lineToRelative(dx = 2.13f, dy = -8.02f)
        // a 6.5 6.5 0 0 1 1.29 -2.8
        arcToRelative(
          a = 6.5f,
          b = 6.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.29f,
          dy1 = -2.8f,
        )
        // c 0.69 -0.84 1.74 -1.19 2.87 -1.32
        curveToRelative(
          dx1 = 0.69f,
          dy1 = -0.84f,
          dx2 = 1.74f,
          dy2 = -1.19f,
          dx3 = 2.87f,
          dy3 = -1.32f,
        )
        // c 0.23 -0.65 0.91 -1.09 1.68 -1.09
        curveToRelative(
          dx1 = 0.23f,
          dy1 = -0.65f,
          dx2 = 0.91f,
          dy2 = -1.09f,
          dx3 = 1.68f,
          dy3 = -1.09f,
        )
        // s 1.45 0.44 1.67 1.08
        reflectiveCurveToRelative(
          dx1 = 1.45f,
          dy1 = 0.44f,
          dx2 = 1.67f,
          dy2 = 1.08f,
        )
        // m 0.08 15.89
        moveToRelative(dx = 0.08f, dy = 15.89f)
        // c 0 0.88 -0.82 1.53 -1.75 1.53
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 0.88f,
          dx2 = -0.82f,
          dy2 = 1.53f,
          dx3 = -1.75f,
          dy3 = 1.53f,
        )
        // s -1.75 -0.65 -1.75 -1.53
        reflectiveCurveToRelative(
          dx1 = -1.75f,
          dy1 = -0.65f,
          dx2 = -1.75f,
          dy2 = -1.53f,
        )
        // c 0 -0.3 0.26 -0.47 0.5 -0.47
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -0.3f,
          dx2 = 0.26f,
          dy2 = -0.47f,
          dx3 = 0.5f,
          dy3 = -0.47f,
        )
        // h 2.5
        horizontalLineToRelative(dx = 2.5f)
        // c 0.24 0 0.5 0.18 0.5 0.47
        curveToRelative(
          dx1 = 0.24f,
          dy1 = 0.0f,
          dx2 = 0.5f,
          dy2 = 0.18f,
          dx3 = 0.5f,
          dy3 = 0.47f,
        )
      }
    }.build().also { _notification = it }
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
        imageVector = HedvigIcons.Notification,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _notification: ImageVector? = null
