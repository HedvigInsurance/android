package com.hedvig.android.design.system.hedvig.icon.colored

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
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ColoredFirstVetWithDot: ImageVector
  get() {
    val current = _firstVet
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.colored.FirstVet",
      defaultWidth = 40.0.dp,
      defaultHeight = 40.0.dp,
      viewportWidth = 40.0f,
      viewportHeight = 40.0f,
    ).apply {
      // M20 4 A16 16 0 1 0 20 36 16 16 0 1 0 20 4z
      path(
        fill = SolidColor(Color(0xFF0061FF)),
      ) {
        // M 20 4
        moveTo(x = 20.0f, y = 4.0f)
        // A 16 16 0 1 0 20 36
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 36.0f,
        )
        // A 16 16 0 1 0 20 4z
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 4.0f,
        )
        close()
      }
      // m28 15.14 -2.75 1.44 -.02 -.17 a3.14 3.14 0 0 0 -2.8 -2.7 34 34 0 0 0 -3.73 -.21 h-.45 a34 34 0 0 0 -3.73 .2 3.14 3.14 0 0 0 -2.8 2.71 31 31 0 0 0 0 7.4 3.14 3.14 0 0 0 2.8 2.71 35 35 0 0 0 3.73 .2 h.45 q1.88 0 3.73 -.2 a3.14 3.14 0 0 0 2.8 -2.71 l.02 -.17 L28 25.08 c.23 .12 .5 -.04 .5 -.3 v-9.35 c0 -.25 -.27 -.4 -.5 -.29 m-5.32 5.66 a1 1 0 0 1 -1.18 .8 l-2 -.34 -.52 2.93 -1 -.17 a1 1 0 0 1 -.82 -1.15 l.35 -1.96 -2.99 -.51 .18 -.98 a1 1 0 0 1 1.17 -.8 l2 .34 .52 -2.93 1 .17 a1 1 0 0 1 .82 1.15 l-.35 1.96 3 .51z
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
      ) {
        // m 28 15.14
        moveToRelative(dx = 28.0f, dy = 15.14f)
        // l -2.75 1.44
        lineToRelative(dx = -2.75f, dy = 1.44f)
        // l -0.02 -0.17
        lineToRelative(dx = -0.02f, dy = -0.17f)
        // a 3.14 3.14 0 0 0 -2.8 -2.7
        arcToRelative(
          a = 3.14f,
          b = 3.14f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.8f,
          dy1 = -2.7f,
        )
        // a 34 34 0 0 0 -3.73 -0.21
        arcToRelative(
          a = 34.0f,
          b = 34.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -3.73f,
          dy1 = -0.21f,
        )
        // h -0.45
        horizontalLineToRelative(dx = -0.45f)
        // a 34 34 0 0 0 -3.73 0.2
        arcToRelative(
          a = 34.0f,
          b = 34.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -3.73f,
          dy1 = 0.2f,
        )
        // a 3.14 3.14 0 0 0 -2.8 2.71
        arcToRelative(
          a = 3.14f,
          b = 3.14f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.8f,
          dy1 = 2.71f,
        )
        // a 31 31 0 0 0 0 7.4
        arcToRelative(
          a = 31.0f,
          b = 31.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 7.4f,
        )
        // a 3.14 3.14 0 0 0 2.8 2.71
        arcToRelative(
          a = 3.14f,
          b = 3.14f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.8f,
          dy1 = 2.71f,
        )
        // a 35 35 0 0 0 3.73 0.2
        arcToRelative(
          a = 35.0f,
          b = 35.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 3.73f,
          dy1 = 0.2f,
        )
        // h 0.45
        horizontalLineToRelative(dx = 0.45f)
        // q 1.88 0 3.73 -0.2
        quadToRelative(
          dx1 = 1.88f,
          dy1 = 0.0f,
          dx2 = 3.73f,
          dy2 = -0.2f,
        )
        // a 3.14 3.14 0 0 0 2.8 -2.71
        arcToRelative(
          a = 3.14f,
          b = 3.14f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.8f,
          dy1 = -2.71f,
        )
        // l 0.02 -0.17
        lineToRelative(dx = 0.02f, dy = -0.17f)
        // L 28 25.08
        lineTo(x = 28.0f, y = 25.08f)
        // c 0.23 0.12 0.5 -0.04 0.5 -0.3
        curveToRelative(
          dx1 = 0.23f,
          dy1 = 0.12f,
          dx2 = 0.5f,
          dy2 = -0.04f,
          dx3 = 0.5f,
          dy3 = -0.3f,
        )
        // v -9.35
        verticalLineToRelative(dy = -9.35f)
        // c 0 -0.25 -0.27 -0.4 -0.5 -0.29
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -0.25f,
          dx2 = -0.27f,
          dy2 = -0.4f,
          dx3 = -0.5f,
          dy3 = -0.29f,
        )
        // m -5.32 5.66
        moveToRelative(dx = -5.32f, dy = 5.66f)
        // a 1 1 0 0 1 -1.18 0.8
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.18f,
          dy1 = 0.8f,
        )
        // l -2 -0.34
        lineToRelative(dx = -2.0f, dy = -0.34f)
        // l -0.52 2.93
        lineToRelative(dx = -0.52f, dy = 2.93f)
        // l -1 -0.17
        lineToRelative(dx = -1.0f, dy = -0.17f)
        // a 1 1 0 0 1 -0.82 -1.15
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.82f,
          dy1 = -1.15f,
        )
        // l 0.35 -1.96
        lineToRelative(dx = 0.35f, dy = -1.96f)
        // l -2.99 -0.51
        lineToRelative(dx = -2.99f, dy = -0.51f)
        // l 0.18 -0.98
        lineToRelative(dx = 0.18f, dy = -0.98f)
        // a 1 1 0 0 1 1.17 -0.8
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.17f,
          dy1 = -0.8f,
        )
        // l 2 0.34
        lineToRelative(dx = 2.0f, dy = 0.34f)
        // l 0.52 -2.93
        lineToRelative(dx = 0.52f, dy = -2.93f)
        // l 1 0.17
        lineToRelative(dx = 1.0f, dy = 0.17f)
        // a 1 1 0 0 1 0.82 1.15
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.82f,
          dy1 = 1.15f,
        )
        // l -0.35 1.96
        lineToRelative(dx = -0.35f, dy = 1.96f)
        // l 3 0.51z
        lineToRelative(dx = 3.0f, dy = 0.51f)
        close()
      }
      // M31 4 A5 5 0 1 0 31 14 5 5 0 1 0 31 4z
      path(
        fill = SolidColor(Color(0xFFFF513A)),
      ) {
        // M 31 4
        moveTo(x = 31.0f, y = 4.0f)
        // A 5 5 0 1 0 31 14
        arcTo(
          horizontalEllipseRadius = 5.0f,
          verticalEllipseRadius = 5.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 31.0f,
          y1 = 14.0f,
        )
        // A 5 5 0 1 0 31 4z
        arcTo(
          horizontalEllipseRadius = 5.0f,
          verticalEllipseRadius = 5.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 31.0f,
          y1 = 4.0f,
        )
        close()
      }
    }.build().also { _firstVet = it }
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
        imageVector = HedvigIcons.ColoredFirstVet,
        contentDescription = null,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _firstVet: ImageVector? = null
