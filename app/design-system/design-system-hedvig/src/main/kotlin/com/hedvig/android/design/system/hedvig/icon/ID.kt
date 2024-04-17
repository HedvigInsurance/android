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
val HedvigIcons.ID: ImageVector
  get() {
    val current = _iD
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ID",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.75 8 A2.75 2.75 0 0 0 19 5.25 H5 A2.75 2.75 0 0 0 2.25 8 v8 A2.75 2.75 0 0 0 5 18.75 h14 A2.75 2.75 0 0 0 21.75 16z M19 6.75 A1.25 1.25 0 0 1 20.25 8 v8 A1.25 1.25 0 0 1 19 17.25 H5 A1.25 1.25 0 0 1 3.75 16 V8 A1.25 1.25 0 0 1 5 6.75z M7.5 11.5 a1.5 1.5 0 1 0 0 -3 1.5 1.5 0 0 0 0 3 m-1.65 1.48 A3 3 0 0 1 7.5 12.5 c.6 0 1.2 .17 1.65 .48 A1.9 1.9 0 0 1 10 14.5 a1 1 0 0 1 -.43 .81 1 1 0 0 1 -.62 .19 h-2.9 a1 1 0 0 1 -.62 -.19 A1 1 0 0 1 5 14.5 c0 -.7 .4 -1.21 .85 -1.52 M12 8.75 a.75 .75 0 0 0 0 1.5 h6 a.75 .75 0 0 0 0 -1.5z M11.25 12 A.75 .75 0 0 1 12 11.25 h6 a.75 .75 0 0 1 0 1.5 h-6 A.75 .75 0 0 1 11.25 12 m4.25 1.75 a.75 .75 0 0 0 0 1.5 H18 a.75 .75 0 0 0 0 -1.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.75 8
        moveTo(x = 21.75f, y = 8.0f)
        // A 2.75 2.75 0 0 0 19 5.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 19.0f,
          y1 = 5.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 2.75 2.75 0 0 0 2.25 8
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 2.25f,
          y1 = 8.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 2.75 2.75 0 0 0 5 18.75
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 5.0f,
          y1 = 18.75f,
        )
        // h 14
        horizontalLineToRelative(dx = 14.0f)
        // A 2.75 2.75 0 0 0 21.75 16z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 21.75f,
          y1 = 16.0f,
        )
        close()
        // M 19 6.75
        moveTo(x = 19.0f, y = 6.75f)
        // A 1.25 1.25 0 0 1 20.25 8
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 20.25f,
          y1 = 8.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // A 1.25 1.25 0 0 1 19 17.25
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.0f,
          y1 = 17.25f,
        )
        // H 5
        horizontalLineTo(x = 5.0f)
        // A 1.25 1.25 0 0 1 3.75 16
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 3.75f,
          y1 = 16.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // A 1.25 1.25 0 0 1 5 6.75z
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 6.75f,
        )
        close()
        // M 7.5 11.5
        moveTo(x = 7.5f, y = 11.5f)
        // a 1.5 1.5 0 1 0 0 -3
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -3.0f,
        )
        // a 1.5 1.5 0 0 0 0 3
        arcToRelative(
          a = 1.5f,
          b = 1.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 3.0f,
        )
        // m -1.65 1.48
        moveToRelative(dx = -1.65f, dy = 1.48f)
        // A 3 3 0 0 1 7.5 12.5
        arcTo(
          horizontalEllipseRadius = 3.0f,
          verticalEllipseRadius = 3.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.5f,
          y1 = 12.5f,
        )
        // c 0.6 0 1.2 0.17 1.65 0.48
        curveToRelative(
          dx1 = 0.6f,
          dy1 = 0.0f,
          dx2 = 1.2f,
          dy2 = 0.17f,
          dx3 = 1.65f,
          dy3 = 0.48f,
        )
        // A 1.9 1.9 0 0 1 10 14.5
        arcTo(
          horizontalEllipseRadius = 1.9f,
          verticalEllipseRadius = 1.9f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 10.0f,
          y1 = 14.5f,
        )
        // a 1 1 0 0 1 -0.43 0.81
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.43f,
          dy1 = 0.81f,
        )
        // a 1 1 0 0 1 -0.62 0.19
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.62f,
          dy1 = 0.19f,
        )
        // h -2.9
        horizontalLineToRelative(dx = -2.9f)
        // a 1 1 0 0 1 -0.62 -0.19
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.62f,
          dy1 = -0.19f,
        )
        // A 1 1 0 0 1 5 14.5
        arcTo(
          horizontalEllipseRadius = 1.0f,
          verticalEllipseRadius = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 14.5f,
        )
        // c 0 -0.7 0.4 -1.21 0.85 -1.52
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -0.7f,
          dx2 = 0.4f,
          dy2 = -1.21f,
          dx3 = 0.85f,
          dy3 = -1.52f,
        )
        // M 12 8.75
        moveTo(x = 12.0f, y = 8.75f)
        // a 0.75 0.75 0 0 0 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // a 0.75 0.75 0 0 0 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
        // M 11.25 12
        moveTo(x = 11.25f, y = 12.0f)
        // A 0.75 0.75 0 0 1 12 11.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 11.25f,
        )
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // a 0.75 0.75 0 0 1 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // h -6
        horizontalLineToRelative(dx = -6.0f)
        // A 0.75 0.75 0 0 1 11.25 12
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 11.25f,
          y1 = 12.0f,
        )
        // m 4.25 1.75
        moveToRelative(dx = 4.25f, dy = 1.75f)
        // a 0.75 0.75 0 0 0 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // H 18
        horizontalLineTo(x = 18.0f)
        // a 0.75 0.75 0 0 0 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
      }
    }.build().also { _iD = it }
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
        imageVector = HedvigIcons.ID,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _iD: ImageVector? = null
