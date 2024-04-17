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
val HedvigIcons.ProfileOutline: ImageVector
  get() {
    val current = _profileOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ProfileOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M15.25 6.75 a3.25 3.25 0 1 1 -6.5 0 3.25 3.25 0 0 1 6.5 0 m1.5 0 a4.75 4.75 0 1 1 -9.5 0 4.75 4.75 0 0 1 9.5 0 M5.5 19.9 c0 -2.9 2.81 -5.4 6.5 -5.4 s6.5 2.5 6.5 5.4 a.5 .5 0 0 1 -.2 .38 1 1 0 0 1 -.61 .22 H6.3 a1 1 0 0 1 -.62 -.22 .5 .5 0 0 1 -.19 -.38 m-1.5 0 c0 -3.96 3.74 -6.9 8 -6.9 s8 2.94 8 6.9 c0 1.3 -1.2 2.1 -2.31 2.1 H6.3 C5.2 22 4 21.2 4 19.9
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 15.25 6.75
        moveTo(x = 15.25f, y = 6.75f)
        // a 3.25 3.25 0 1 1 -6.5 0
        arcToRelative(
          a = 3.25f,
          b = 3.25f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -6.5f,
          dy1 = 0.0f,
        )
        // a 3.25 3.25 0 0 1 6.5 0
        arcToRelative(
          a = 3.25f,
          b = 3.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 6.5f,
          dy1 = 0.0f,
        )
        // m 1.5 0
        moveToRelative(dx = 1.5f, dy = 0.0f)
        // a 4.75 4.75 0 1 1 -9.5 0
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -9.5f,
          dy1 = 0.0f,
        )
        // a 4.75 4.75 0 0 1 9.5 0
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 9.5f,
          dy1 = 0.0f,
        )
        // M 5.5 19.9
        moveTo(x = 5.5f, y = 19.9f)
        // c 0 -2.9 2.81 -5.4 6.5 -5.4
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -2.9f,
          dx2 = 2.81f,
          dy2 = -5.4f,
          dx3 = 6.5f,
          dy3 = -5.4f,
        )
        // s 6.5 2.5 6.5 5.4
        reflectiveCurveToRelative(
          dx1 = 6.5f,
          dy1 = 2.5f,
          dx2 = 6.5f,
          dy2 = 5.4f,
        )
        // a 0.5 0.5 0 0 1 -0.2 0.38
        arcToRelative(
          a = 0.5f,
          b = 0.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.2f,
          dy1 = 0.38f,
        )
        // a 1 1 0 0 1 -0.61 0.22
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.61f,
          dy1 = 0.22f,
        )
        // H 6.3
        horizontalLineTo(x = 6.3f)
        // a 1 1 0 0 1 -0.62 -0.22
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.62f,
          dy1 = -0.22f,
        )
        // a 0.5 0.5 0 0 1 -0.19 -0.38
        arcToRelative(
          a = 0.5f,
          b = 0.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.19f,
          dy1 = -0.38f,
        )
        // m -1.5 0
        moveToRelative(dx = -1.5f, dy = 0.0f)
        // c 0 -3.96 3.74 -6.9 8 -6.9
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -3.96f,
          dx2 = 3.74f,
          dy2 = -6.9f,
          dx3 = 8.0f,
          dy3 = -6.9f,
        )
        // s 8 2.94 8 6.9
        reflectiveCurveToRelative(
          dx1 = 8.0f,
          dy1 = 2.94f,
          dx2 = 8.0f,
          dy2 = 6.9f,
        )
        // c 0 1.3 -1.2 2.1 -2.31 2.1
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 1.3f,
          dx2 = -1.2f,
          dy2 = 2.1f,
          dx3 = -2.31f,
          dy3 = 2.1f,
        )
        // H 6.3
        horizontalLineTo(x = 6.3f)
        // C 5.2 22 4 21.2 4 19.9
        curveTo(
          x1 = 5.2f,
          y1 = 22.0f,
          x2 = 4.0f,
          y2 = 21.2f,
          x3 = 4.0f,
          y3 = 19.9f,
        )
      }
    }.build().also { _profileOutline = it }
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
        imageVector = HedvigIcons.ProfileOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _profileOutline: ImageVector? = null
