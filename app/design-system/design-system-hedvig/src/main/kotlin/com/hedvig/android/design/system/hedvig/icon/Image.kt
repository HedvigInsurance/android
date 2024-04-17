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

val Image: ImageVector
  get() {
    val current = _image
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Image",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19 5.25 A2.75 2.75 0 0 1 21.75 8 v8 A2.75 2.75 0 0 1 19 18.75 H5 A2.75 2.75 0 0 1 2.25 16 V8 A2.75 2.75 0 0 1 5 5.25z M20.25 8 A1.25 1.25 0 0 0 19 6.75 H5 A1.25 1.25 0 0 0 3.75 8 v8.13 l4.4 -5.19 a1.75 1.75 0 0 1 2.7 .04 l2.54 3.18 a.25 .25 0 0 0 .37 .02 l1 -1 a1.75 1.75 0 0 1 2.48 0 l3 3 L20.25 16z m-1.07 9.24 -3 -3 a.25 .25 0 0 0 -.36 0 l-1 1 a1.75 1.75 0 0 1 -2.6 -.14 l-2.55 -3.18 a.25 .25 0 0 0 -.38 0 l-4.5 5.31 L5 17.25 h14z M19 9.5 a1.5 1.5 0 1 1 -3 0 1.5 1.5 0 0 1 3 0
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 19 5.25
        moveTo(x = 19.0f, y = 5.25f)
        // A 2.75 2.75 0 0 1 21.75 8
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 21.75f,
          y1 = 8.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 2.75 2.75 0 0 1 19 18.75
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.0f,
          y1 = 18.75f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 2.75 2.75 0 0 1 2.25 16
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 2.25f,
          y1 = 16.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // A 2.75 2.75 0 0 1 5 5.25z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 5.25f,
        )
        close()
        // M 20.25 8
        moveTo(x = 20.25f, y = 8.0f)
        // A 1.25 1.25 0 0 0 19 6.75
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 19.0f,
          y1 = 6.75f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 1.25 1.25 0 0 0 3.75 8
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 3.75f,
          y1 = 8.0f,
        )
        // v 8.13
        verticalLineToRelative(dy = 8.13f)
        // l 4.4 -5.19
        lineToRelative(dx = 4.4f, dy = -5.19f)
        // a 1.75 1.75 0 0 1 2.7 0.04
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.7f,
          dy1 = 0.04f,
        )
        // l 2.54 3.18
        lineToRelative(dx = 2.54f, dy = 3.18f)
        // a 0.25 0.25 0 0 0 0.37 0.02
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.37f,
          dy1 = 0.02f,
        )
        // l 1 -1
        lineToRelative(dx = 1.0f, dy = -1.0f)
        // a 1.75 1.75 0 0 1 2.48 0
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.48f,
          dy1 = 0.0f,
        )
        // l 3 3
        lineToRelative(dx = 3.0f, dy = 3.0f)
        // L 20.25 16z
        lineTo(x = 20.25f, y = 16.0f)
        close()
        // m -1.07 9.24
        moveToRelative(dx = -1.07f, dy = 9.24f)
        // l -3 -3
        lineToRelative(dx = -3.0f, dy = -3.0f)
        // a 0.25 0.25 0 0 0 -0.36 0
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.36f,
          dy1 = 0.0f,
        )
        // l -1 1
        lineToRelative(dx = -1.0f, dy = 1.0f)
        // a 1.75 1.75 0 0 1 -2.6 -0.14
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.6f,
          dy1 = -0.14f,
        )
        // l -2.55 -3.18
        lineToRelative(dx = -2.55f, dy = -3.18f)
        // a 0.25 0.25 0 0 0 -0.38 0
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.38f,
          dy1 = 0.0f,
        )
        // l -4.5 5.31
        lineToRelative(dx = -4.5f, dy = 5.31f)
        // L 5 17.25
        lineTo(x = 5.0f, y = 17.25f)
        // h 14z
        horizontalLineToRelative(dx = 14.0f)
        close()
        // M 19 9.5
        moveTo(x = 19.0f, y = 9.5f)
        // a 1.5 1.5 0 1 1 -3 0
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -3.0f,
          dy1 = 0.0f,
        )
        // a 1.5 1.5 0 0 1 3 0
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 3.0f,
          dy1 = 0.0f,
        )
      }
    }.build().also { _image = it }
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
        imageVector = Image,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _image: ImageVector? = null
