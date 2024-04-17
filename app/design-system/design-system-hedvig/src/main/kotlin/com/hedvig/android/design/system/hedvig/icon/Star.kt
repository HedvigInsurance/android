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
val HedvigIcons.Star: ImageVector
  get() {
    val current = _star
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Star",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.32 3 a1.91 1.91 0 0 1 3.36 0 l1.88 3.35 a2 2 0 0 0 1.27 .97 l3.63 .82 c1.46 .32 2.04 2.17 1.04 3.33 l-2.48 2.89 a2 2 0 0 0 -.48 1.55 l.37 3.86 c.15 1.54 -1.35 2.68 -2.72 2.05 l-3.4 -1.56 a1.9 1.9 0 0 0 -1.58 0 l-3.4 1.56 c-1.37 .63 -2.87 -.5 -2.72 -2.05 l.37 -3.86 a2 2 0 0 0 -.48 -1.55 l-2.48 -2.9 c-1 -1.15 -.42 -3 1.04 -3.32 l3.63 -.82 a2 2 0 0 0 1.27 -.97z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 10.32 3
        moveTo(x = 10.32f, y = 3.0f)
        // a 1.91 1.91 0 0 1 3.36 0
        arcToRelative(
          a = 1.91f,
          b = 1.91f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 3.36f,
          dy1 = 0.0f,
        )
        // l 1.88 3.35
        lineToRelative(dx = 1.88f, dy = 3.35f)
        // a 2 2 0 0 0 1.27 0.97
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.27f,
          dy1 = 0.97f,
        )
        // l 3.63 0.82
        lineToRelative(dx = 3.63f, dy = 0.82f)
        // c 1.46 0.32 2.04 2.17 1.04 3.33
        curveToRelative(
          dx1 = 1.46f,
          dy1 = 0.32f,
          dx2 = 2.04f,
          dy2 = 2.17f,
          dx3 = 1.04f,
          dy3 = 3.33f,
        )
        // l -2.48 2.89
        lineToRelative(dx = -2.48f, dy = 2.89f)
        // a 2 2 0 0 0 -0.48 1.55
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.48f,
          dy1 = 1.55f,
        )
        // l 0.37 3.86
        lineToRelative(dx = 0.37f, dy = 3.86f)
        // c 0.15 1.54 -1.35 2.68 -2.72 2.05
        curveToRelative(
          dx1 = 0.15f,
          dy1 = 1.54f,
          dx2 = -1.35f,
          dy2 = 2.68f,
          dx3 = -2.72f,
          dy3 = 2.05f,
        )
        // l -3.4 -1.56
        lineToRelative(dx = -3.4f, dy = -1.56f)
        // a 1.9 1.9 0 0 0 -1.58 0
        arcToRelative(
          a = 1.9f,
          b = 1.9f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.58f,
          dy1 = 0.0f,
        )
        // l -3.4 1.56
        lineToRelative(dx = -3.4f, dy = 1.56f)
        // c -1.37 0.63 -2.87 -0.5 -2.72 -2.05
        curveToRelative(
          dx1 = -1.37f,
          dy1 = 0.63f,
          dx2 = -2.87f,
          dy2 = -0.5f,
          dx3 = -2.72f,
          dy3 = -2.05f,
        )
        // l 0.37 -3.86
        lineToRelative(dx = 0.37f, dy = -3.86f)
        // a 2 2 0 0 0 -0.48 -1.55
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.48f,
          dy1 = -1.55f,
        )
        // l -2.48 -2.9
        lineToRelative(dx = -2.48f, dy = -2.9f)
        // c -1 -1.15 -0.42 -3 1.04 -3.32
        curveToRelative(
          dx1 = -1.0f,
          dy1 = -1.15f,
          dx2 = -0.42f,
          dy2 = -3.0f,
          dx3 = 1.04f,
          dy3 = -3.32f,
        )
        // l 3.63 -0.82
        lineToRelative(dx = 3.63f, dy = -0.82f)
        // a 2 2 0 0 0 1.27 -0.97z
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.27f,
          dy1 = -0.97f,
        )
        close()
      }
    }.build().also { _star = it }
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
        imageVector = Star,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _star: ImageVector? = null
