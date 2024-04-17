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
val HedvigIcons.Camera: ImageVector
  get() {
    val current = _camera
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Camera",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 9 A2.75 2.75 0 0 0 19 6.25 h-1.87 a1.3 1.3 0 0 1 -1.07 -.6 l-.33 -.56 a2.8 2.8 0 0 0 -2.36 -1.34 h-2.74 a2.8 2.8 0 0 0 -2.36 1.34 L7.94 5.64 a1.3 1.3 0 0 1 -1.07 .61 H5 A2.75 2.75 0 0 0 2.25 9 v8 A2.75 2.75 0 0 0 5 19.75 h14 A2.75 2.75 0 0 0 21.75 17z M19 7.75 A1.25 1.25 0 0 1 20.25 9 v8 A1.25 1.25 0 0 1 19 18.25 H5 A1.25 1.25 0 0 1 3.75 17 V9 A1.25 1.25 0 0 1 5 7.75 h1.87 c.96 0 1.86 -.5 2.36 -1.34 l.33 -.55 a1.3 1.3 0 0 1 1.07 -.61 h2.74 a1.3 1.3 0 0 1 1.07 .6 l.33 .56 a2.8 2.8 0 0 0 2.36 1.34z m-5 4.75 a2 2 0 1 1 -4 0 2 2 0 0 1 4 0 m1.5 0 a3.5 3.5 0 1 1 -7 0 3.5 3.5 0 0 1 7 0
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.75 9
        moveTo(x = 21.75f, y = 9.0f)
        // A 2.75 2.75 0 0 0 19 6.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 19.0f,
          y1 = 6.25f,
        )
        // h -1.87
        horizontalLineToRelative(dx = -1.87f)
        // a 1.3 1.3 0 0 1 -1.07 -0.6
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.07f,
          dy1 = -0.6f,
        )
        // l -0.33 -0.56
        lineToRelative(dx = -0.33f, dy = -0.56f)
        // a 2.8 2.8 0 0 0 -2.36 -1.34
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.36f,
          dy1 = -1.34f,
        )
        // h -2.74
        horizontalLineToRelative(dx = -2.74f)
        // a 2.8 2.8 0 0 0 -2.36 1.34
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.36f,
          dy1 = 1.34f,
        )
        // L 7.94 5.64
        lineTo(x = 7.94f, y = 5.64f)
        // a 1.3 1.3 0 0 1 -1.07 0.61
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.07f,
          dy1 = 0.61f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 2.75 2.75 0 0 0 2.25 9
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 2.25f,
          y1 = 9.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 2.75 2.75 0 0 0 5 19.75
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 5.0f,
          y1 = 19.75f,
        )
        // h 14
        horizontalLineToRelative(dx = 14.0f)
        // A 2.75 2.75 0 0 0 21.75 17z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 21.75f,
          y1 = 17.0f,
        )
        close()
        // M 19 7.75
        moveTo(x = 19.0f, y = 7.75f)
        // A 1.25 1.25 0 0 1 20.25 9
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 20.25f,
          y1 = 9.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 1.25 1.25 0 0 1 19 18.25
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.0f,
          y1 = 18.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 1.25 1.25 0 0 1 3.75 17
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 3.75f,
          y1 = 17.0f,
        )
        // V 9
        verticalLineTo(y = 9.0f)
        // A 1.25 1.25 0 0 1 5 7.75
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 7.75f,
        )
        // h 1.87
        horizontalLineToRelative(dx = 1.87f)
        // c 0.96 0 1.86 -0.5 2.36 -1.34
        curveToRelative(
          dx1 = 0.96f,
          dy1 = 0.0f,
          dx2 = 1.86f,
          dy2 = -0.5f,
          dx3 = 2.36f,
          dy3 = -1.34f,
        )
        // l 0.33 -0.55
        lineToRelative(dx = 0.33f, dy = -0.55f)
        // a 1.3 1.3 0 0 1 1.07 -0.61
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.07f,
          dy1 = -0.61f,
        )
        // h 2.74
        horizontalLineToRelative(dx = 2.74f)
        // a 1.3 1.3 0 0 1 1.07 0.6
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.07f,
          dy1 = 0.6f,
        )
        // l 0.33 0.56
        lineToRelative(dx = 0.33f, dy = 0.56f)
        // a 2.8 2.8 0 0 0 2.36 1.34z
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.36f,
          dy1 = 1.34f,
        )
        close()
        // m -5 4.75
        moveToRelative(dx = -5.0f, dy = 4.75f)
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
    }.build().also { _camera = it }
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
        imageVector = HedvigIcons.Camera,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _camera: ImageVector? = null
