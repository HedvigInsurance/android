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
val HedvigIcons.ShieldOutline: ImageVector
  get() {
    val current = _shieldOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ShieldOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M18.5 12 c0 1.72 -.86 3.34 -2.13 4.77 a18 18 0 0 1 -3.87 3.21 .9 .9 0 0 1 -1 0 18 18 0 0 1 -3.87 -3.2 C6.36 15.33 5.5 13.71 5.5 12 V7.08 a1.5 1.5 0 0 1 .97 -1.4 l5 -1.88 a1.5 1.5 0 0 1 1.06 0 l5 1.87 a1.5 1.5 0 0 1 .97 1.4z m-7.8 9.25 c.8 .5 1.8 .5 2.6 0 C15.56 19.83 20 16.47 20 12 V7.08 a3 3 0 0 0 -1.95 -2.81 l-5 -1.87 a3 3 0 0 0 -2.1 0 l-5 1.87 A3 3 0 0 0 4 7.07 V12 c0 4.47 4.44 7.83 6.7 9.25 m4.98 -10.72 a.75 .75 0 1 0 -1.06 -1.06 l-3.33 3.3 a.25 .25 0 0 1 -.35 0 l-1.26 -1.25 a.75 .75 0 1 0 -1.06 1.06 l1.26 1.25 a1.75 1.75 0 0 0 2.47 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 18.5 12
        moveTo(x = 18.5f, y = 12.0f)
        // c 0 1.72 -0.86 3.34 -2.13 4.77
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 1.72f,
          dx2 = -0.86f,
          dy2 = 3.34f,
          dx3 = -2.13f,
          dy3 = 4.77f,
        )
        // a 18 18 0 0 1 -3.87 3.21
        arcToRelative(
          a = 18.0f,
          b = 18.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -3.87f,
          dy1 = 3.21f,
        )
        // a 0.9 0.9 0 0 1 -1 0
        arcToRelative(
          a = 0.9f,
          b = 0.9f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.0f,
          dy1 = 0.0f,
        )
        // a 18 18 0 0 1 -3.87 -3.2
        arcToRelative(
          a = 18.0f,
          b = 18.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -3.87f,
          dy1 = -3.2f,
        )
        // C 6.36 15.33 5.5 13.71 5.5 12
        curveTo(
          x1 = 6.36f,
          y1 = 15.33f,
          x2 = 5.5f,
          y2 = 13.71f,
          x3 = 5.5f,
          y3 = 12.0f,
        )
        // V 7.08
        verticalLineTo(y = 7.08f)
        // a 1.5 1.5 0 0 1 0.97 -1.4
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.97f,
          dy1 = -1.4f,
        )
        // l 5 -1.88
        lineToRelative(dx = 5.0f, dy = -1.88f)
        // a 1.5 1.5 0 0 1 1.06 0
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 0.0f,
        )
        // l 5 1.87
        lineToRelative(dx = 5.0f, dy = 1.87f)
        // a 1.5 1.5 0 0 1 0.97 1.4z
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.97f,
          dy1 = 1.4f,
        )
        close()
        // m -7.8 9.25
        moveToRelative(dx = -7.8f, dy = 9.25f)
        // c 0.8 0.5 1.8 0.5 2.6 0
        curveToRelative(
          dx1 = 0.8f,
          dy1 = 0.5f,
          dx2 = 1.8f,
          dy2 = 0.5f,
          dx3 = 2.6f,
          dy3 = 0.0f,
        )
        // C 15.56 19.83 20 16.47 20 12
        curveTo(
          x1 = 15.56f,
          y1 = 19.83f,
          x2 = 20.0f,
          y2 = 16.47f,
          x3 = 20.0f,
          y3 = 12.0f,
        )
        // V 7.08
        verticalLineTo(y = 7.08f)
        // a 3 3 0 0 0 -1.95 -2.81
        arcToRelative(
          a = 3.0f,
          b = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.95f,
          dy1 = -2.81f,
        )
        // l -5 -1.87
        lineToRelative(dx = -5.0f, dy = -1.87f)
        // a 3 3 0 0 0 -2.1 0
        arcToRelative(
          a = 3.0f,
          b = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.1f,
          dy1 = 0.0f,
        )
        // l -5 1.87
        lineToRelative(dx = -5.0f, dy = 1.87f)
        // A 3 3 0 0 0 4 7.07
        arcTo(
          horizontalEllipseRadius = 3.0f,
          verticalEllipseRadius = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 4.0f,
          y1 = 7.07f,
        )
        // V 12
        verticalLineTo(y = 12.0f)
        // c 0 4.47 4.44 7.83 6.7 9.25
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 4.47f,
          dx2 = 4.44f,
          dy2 = 7.83f,
          dx3 = 6.7f,
          dy3 = 9.25f,
        )
        // m 4.98 -10.72
        moveToRelative(dx = 4.98f, dy = -10.72f)
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
    }.build().also { _shieldOutline = it }
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
        imageVector = HedvigIcons.ShieldOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _shieldOutline: ImageVector? = null
