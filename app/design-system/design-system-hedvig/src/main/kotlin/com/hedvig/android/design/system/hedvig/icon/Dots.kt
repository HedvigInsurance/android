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

val Dots: ImageVector
  get() {
    val current = _dots
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Dots",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.23 13.5 a1.4 1.4 0 0 1 -1.06 -.44 A1.4 1.4 0 0 1 4.73 12 a1.4 1.4 0 0 1 .44 -1.06 1.4 1.4 0 0 1 1.06 -.44 1.4 1.4 0 0 1 1.06 .44 A1.4 1.4 0 0 1 7.73 12 a1.4 1.4 0 0 1 -.44 1.06 1.4 1.4 0 0 1 -1.06 .44 m5.77 0 a1.4 1.4 0 0 1 -1.06 -.44 A1.4 1.4 0 0 1 10.5 12 a1.4 1.4 0 0 1 .44 -1.06 A1.4 1.4 0 0 1 12 10.5 a1.4 1.4 0 0 1 1.06 .44 A1.4 1.4 0 0 1 13.5 12 a1.4 1.4 0 0 1 -.44 1.06 A1.4 1.4 0 0 1 12 13.5 m5.77 0 a1.4 1.4 0 0 1 -1.06 -.44 A1.4 1.4 0 0 1 16.27 12 a1.4 1.4 0 0 1 .44 -1.06 1.4 1.4 0 0 1 1.06 -.44 1.4 1.4 0 0 1 1.06 .44 A1.4 1.4 0 0 1 19.27 12 a1.4 1.4 0 0 1 -.44 1.06 1.4 1.4 0 0 1 -1.06 .44
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 6.23 13.5
        moveTo(x = 6.23f, y = 13.5f)
        // a 1.4 1.4 0 0 1 -1.06 -0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -0.44f,
        )
        // A 1.4 1.4 0 0 1 4.73 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 4.73f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 0.44 -1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.44f,
          dy1 = -1.06f,
        )
        // a 1.4 1.4 0 0 1 1.06 -0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -0.44f,
        )
        // a 1.4 1.4 0 0 1 1.06 0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 0.44f,
        )
        // A 1.4 1.4 0 0 1 7.73 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.73f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 -0.44 1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.44f,
          dy1 = 1.06f,
        )
        // a 1.4 1.4 0 0 1 -1.06 0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = 0.44f,
        )
        // m 5.77 0
        moveToRelative(dx = 5.77f, dy = 0.0f)
        // a 1.4 1.4 0 0 1 -1.06 -0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -0.44f,
        )
        // A 1.4 1.4 0 0 1 10.5 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 10.5f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 0.44 -1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.44f,
          dy1 = -1.06f,
        )
        // A 1.4 1.4 0 0 1 12 10.5
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 10.5f,
        )
        // a 1.4 1.4 0 0 1 1.06 0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 0.44f,
        )
        // A 1.4 1.4 0 0 1 13.5 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 13.5f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 -0.44 1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.44f,
          dy1 = 1.06f,
        )
        // A 1.4 1.4 0 0 1 12 13.5
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 13.5f,
        )
        // m 5.77 0
        moveToRelative(dx = 5.77f, dy = 0.0f)
        // a 1.4 1.4 0 0 1 -1.06 -0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -0.44f,
        )
        // A 1.4 1.4 0 0 1 16.27 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 16.27f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 0.44 -1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.44f,
          dy1 = -1.06f,
        )
        // a 1.4 1.4 0 0 1 1.06 -0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -0.44f,
        )
        // a 1.4 1.4 0 0 1 1.06 0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 0.44f,
        )
        // A 1.4 1.4 0 0 1 19.27 12
        arcTo(
          horizontalEllipseRadius = 1.4f,
          verticalEllipseRadius = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.27f,
          y1 = 12.0f,
        )
        // a 1.4 1.4 0 0 1 -0.44 1.06
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.44f,
          dy1 = 1.06f,
        )
        // a 1.4 1.4 0 0 1 -1.06 0.44
        arcToRelative(
          a = 1.4f,
          b = 1.4f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = 0.44f,
        )
      }
    }.build().also { _dots = it }
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
        imageVector = Dots,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _dots: ImageVector? = null
