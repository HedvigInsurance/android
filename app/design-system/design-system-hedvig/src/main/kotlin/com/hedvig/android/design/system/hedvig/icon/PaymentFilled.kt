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
val HedvigIcons.PaymentFilled: ImageVector
  get() {
    val current = _paymentFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.PaymentFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.5 2.25 A2.75 2.75 0 0 0 4.75 5 v13.85 c0 2.01 2.1 3.34 3.92 2.49 l.57 -.27 a1.3 1.3 0 0 1 1.04 -.01 l.6 .26 a2.8 2.8 0 0 0 2.24 0 l.6 -.26 a1.3 1.3 0 0 1 1.04 0 l.57 .28 a2.75 2.75 0 0 0 3.92 -2.5 V5 a2.75 2.75 0 0 0 -2.75 -2.75z m8.25 6 A.75 .75 0 0 0 15 7.5 H9 A.75 .75 0 0 0 9 9 h6 a.75 .75 0 0 0 .75 -.75 m-2 2.5 A.75 .75 0 0 0 13 10 H9 a.75 .75 0 0 0 0 1.5 h4 a.75 .75 0 0 0 .75 -.75 M15 12.5 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 0 -1.5z m-1.25 3.25 A.75 .75 0 0 0 13 15 H9 a.75 .75 0 0 0 0 1.5 h4 a.75 .75 0 0 0 .75 -.75
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 7.5 2.25
        moveTo(x = 7.5f, y = 2.25f)
        // A 2.75 2.75 0 0 0 4.75 5
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 4.75f,
          y1 = 5.0f,
        )
        // v 13.85
        verticalLineToRelative(dy = 13.85f)
        // c 0 2.01 2.1 3.34 3.92 2.49
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 2.01f,
          dx2 = 2.1f,
          dy2 = 3.34f,
          dx3 = 3.92f,
          dy3 = 2.49f,
        )
        // l 0.57 -0.27
        lineToRelative(dx = 0.57f, dy = -0.27f)
        // a 1.3 1.3 0 0 1 1.04 -0.01
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.04f,
          dy1 = -0.01f,
        )
        // l 0.6 0.26
        lineToRelative(dx = 0.6f, dy = 0.26f)
        // a 2.8 2.8 0 0 0 2.24 0
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.24f,
          dy1 = 0.0f,
        )
        // l 0.6 -0.26
        lineToRelative(dx = 0.6f, dy = -0.26f)
        // a 1.3 1.3 0 0 1 1.04 0
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.04f,
          dy1 = 0.0f,
        )
        // l 0.57 0.28
        lineToRelative(dx = 0.57f, dy = 0.28f)
        // a 2.75 2.75 0 0 0 3.92 -2.5
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 3.92f,
          dy1 = -2.5f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // a 2.75 2.75 0 0 0 -2.75 -2.75z
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.75f,
          dy1 = -2.75f,
        )
        close()
        // m 8.25 6
        moveToRelative(dx = 8.25f, dy = 6.0f)
        // A 0.75 0.75 0 0 0 15 7.5
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 15.0f,
          y1 = 7.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // A 0.75 0.75 0 0 0 9 9
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 9.0f,
          y1 = 9.0f,
        )
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // a 0.75 0.75 0 0 0 0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.75f,
          dy1 = -0.75f,
        )
        // m -2 2.5
        moveToRelative(dx = -2.0f, dy = 2.5f)
        // A 0.75 0.75 0 0 0 13 10
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 13.0f,
          y1 = 10.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
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
        // h 4
        horizontalLineToRelative(dx = 4.0f)
        // a 0.75 0.75 0 0 0 0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.75f,
          dy1 = -0.75f,
        )
        // M 15 12.5
        moveTo(x = 15.0f, y = 12.5f)
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
        // H 9
        horizontalLineTo(x = 9.0f)
        // a 0.75 0.75 0 0 1 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
        // m -1.25 3.25
        moveToRelative(dx = -1.25f, dy = 3.25f)
        // A 0.75 0.75 0 0 0 13 15
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 13.0f,
          y1 = 15.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
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
        // h 4
        horizontalLineToRelative(dx = 4.0f)
        // a 0.75 0.75 0 0 0 0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.75f,
          dy1 = -0.75f,
        )
      }
    }.build().also { _paymentFilled = it }
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
        imageVector = PaymentFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _paymentFilled: ImageVector? = null
