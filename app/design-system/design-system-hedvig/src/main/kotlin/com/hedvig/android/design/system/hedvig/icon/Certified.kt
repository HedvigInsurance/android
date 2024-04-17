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
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Certified: ImageVector
  get() {
    val current = _certified
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Certified",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.34 2.52 a2 2 0 0 0 -2.68 0 L9.64 3.46 a2 2 0 0 1 -1.41 .52 L6.85 3.93 A2 2 0 0 0 4.8 5.67 L4.6 7.05 a2 2 0 0 1 -.75 1.32 L2.77 9.22 a2.03 2.03 0 0 0 -.46 2.67 l.73 1.18 a2 2 0 0 1 .26 1.5 l-.29 1.37 a2 2 0 0 0 1.34 2.34 l1.31 .43 a2 2 0 0 1 1.15 .98 l.65 1.23 a2 2 0 0 0 2.51 .93 l1.28 -.53 a2 2 0 0 1 1.5 0 l1.28 .53 c.95 .4 2.04 0 2.51 -.93 l.65 -1.23 a2 2 0 0 1 1.15 -.98 l1.31 -.43 a2 2 0 0 0 1.34 -2.34 l-.3 -1.36 a2 2 0 0 1 .27 -1.5 l.73 -1.19 a2.03 2.03 0 0 0 -.46 -2.67 l-1.09 -.85 a2 2 0 0 1 -.75 -1.32 L19.2 5.67 a2 2 0 0 0 -2.05 -1.74 l-1.38 .05 a2 2 0 0 1 -1.41 -.52z m2.19 8.51 a.75 .75 0 0 0 -1.06 -1.06 l-3.33 3.3 a.25 .25 0 0 1 -.35 0 l-1.26 -1.25 a.75 .75 0 1 0 -1.06 1.06 l1.26 1.25 a1.75 1.75 0 0 0 2.47 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 13.34 2.52
        moveTo(x = 13.34f, y = 2.52f)
        // a 2 2 0 0 0 -2.68 0
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.68f,
          dy1 = 0.0f,
        )
        // L 9.64 3.46
        lineTo(x = 9.64f, y = 3.46f)
        // a 2 2 0 0 1 -1.41 0.52
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.41f,
          dy1 = 0.52f,
        )
        // L 6.85 3.93
        lineTo(x = 6.85f, y = 3.93f)
        // A 2 2 0 0 0 4.8 5.67
        arcTo(
          horizontalEllipseRadius = 2.0f,
          verticalEllipseRadius = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 4.8f,
          y1 = 5.67f,
        )
        // L 4.6 7.05
        lineTo(x = 4.6f, y = 7.05f)
        // a 2 2 0 0 1 -0.75 1.32
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = 1.32f,
        )
        // L 2.77 9.22
        lineTo(x = 2.77f, y = 9.22f)
        // a 2.03 2.03 0 0 0 -0.46 2.67
        arcToRelative(
          a = 2.03f,
          b = 2.03f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.46f,
          dy1 = 2.67f,
        )
        // l 0.73 1.18
        lineToRelative(dx = 0.73f, dy = 1.18f)
        // a 2 2 0 0 1 0.26 1.5
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.26f,
          dy1 = 1.5f,
        )
        // l -0.29 1.37
        lineToRelative(dx = -0.29f, dy = 1.37f)
        // a 2 2 0 0 0 1.34 2.34
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.34f,
          dy1 = 2.34f,
        )
        // l 1.31 0.43
        lineToRelative(dx = 1.31f, dy = 0.43f)
        // a 2 2 0 0 1 1.15 0.98
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.15f,
          dy1 = 0.98f,
        )
        // l 0.65 1.23
        lineToRelative(dx = 0.65f, dy = 1.23f)
        // a 2 2 0 0 0 2.51 0.93
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.51f,
          dy1 = 0.93f,
        )
        // l 1.28 -0.53
        lineToRelative(dx = 1.28f, dy = -0.53f)
        // a 2 2 0 0 1 1.5 0
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // l 1.28 0.53
        lineToRelative(dx = 1.28f, dy = 0.53f)
        // c 0.95 0.4 2.04 0 2.51 -0.93
        curveToRelative(
          dx1 = 0.95f,
          dy1 = 0.4f,
          dx2 = 2.04f,
          dy2 = 0.0f,
          dx3 = 2.51f,
          dy3 = -0.93f,
        )
        // l 0.65 -1.23
        lineToRelative(dx = 0.65f, dy = -1.23f)
        // a 2 2 0 0 1 1.15 -0.98
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.15f,
          dy1 = -0.98f,
        )
        // l 1.31 -0.43
        lineToRelative(dx = 1.31f, dy = -0.43f)
        // a 2 2 0 0 0 1.34 -2.34
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.34f,
          dy1 = -2.34f,
        )
        // l -0.3 -1.36
        lineToRelative(dx = -0.3f, dy = -1.36f)
        // a 2 2 0 0 1 0.27 -1.5
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.27f,
          dy1 = -1.5f,
        )
        // l 0.73 -1.19
        lineToRelative(dx = 0.73f, dy = -1.19f)
        // a 2.03 2.03 0 0 0 -0.46 -2.67
        arcToRelative(
          a = 2.03f,
          b = 2.03f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.46f,
          dy1 = -2.67f,
        )
        // l -1.09 -0.85
        lineToRelative(dx = -1.09f, dy = -0.85f)
        // a 2 2 0 0 1 -0.75 -1.32
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = -1.32f,
        )
        // L 19.2 5.67
        lineTo(x = 19.2f, y = 5.67f)
        // a 2 2 0 0 0 -2.05 -1.74
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.05f,
          dy1 = -1.74f,
        )
        // l -1.38 0.05
        lineToRelative(dx = -1.38f, dy = 0.05f)
        // a 2 2 0 0 1 -1.41 -0.52z
        arcToRelative(
          a = 2.0f,
          b = 2.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.41f,
          dy1 = -0.52f,
        )
        close()
        // m 2.19 8.51
        moveToRelative(dx = 2.19f, dy = 8.51f)
        // a 0.75 0.75 0 0 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
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
    }.build().also { _certified = it }
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
        imageVector = HedvigIcons.Certified,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _certified: ImageVector? = null
