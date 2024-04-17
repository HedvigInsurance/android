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
val HedvigIcons.PaymentOutline: ImageVector
  get() {
    val current = _paymentOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.PaymentOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M4.75 5 A2.75 2.75 0 0 1 7.5 2.25 h9 A2.75 2.75 0 0 1 19.25 5 v13.85 c0 2.01 -2.1 3.34 -3.92 2.49 l-.57 -.27 a1.3 1.3 0 0 0 -1.04 -.01 l-.6 .26 a2.8 2.8 0 0 1 -2.24 0 l-.6 -.26 a1.3 1.3 0 0 0 -1.04 0 l-.57 .28 a2.75 2.75 0 0 1 -3.92 -2.5z M7.5 3.75 A1.25 1.25 0 0 0 6.25 5 v13.85 a1.25 1.25 0 0 0 1.78 1.13 l.57 -.27 a2.8 2.8 0 0 1 2.3 -.02 l.6 .26 a1.3 1.3 0 0 0 1 0 l.6 -.26 a2.8 2.8 0 0 1 2.3 .02 l.57 .27 a1.25 1.25 0 0 0 1.78 -1.13 V5 a1.25 1.25 0 0 0 -1.25 -1.25z m.75 4.5 A.75 .75 0 0 1 9 7.5 h6 A.75 .75 0 0 1 15 9 H9 a.75 .75 0 0 1 -.75 -.75 m0 2.5 A.75 .75 0 0 1 9 10 h4 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 -.75 -.75 m0 2.5 A.75 .75 0 0 1 9 12.5 h6 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 -.75 -.75 m0 2.5 A.75 .75 0 0 1 9 15 h4 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 -.75 -.75
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 4.75 5
        moveTo(x = 4.75f, y = 5.0f)
        // A 2.75 2.75 0 0 1 7.5 2.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.5f,
          y1 = 2.25f,
        )
        // h 9
        horizontalLineToRelative(dx = 9.0f)
        // A 2.75 2.75 0 0 1 19.25 5
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.25f,
          y1 = 5.0f,
        )
        // v 13.85
        verticalLineToRelative(dy = 13.85f)
        // c 0 2.01 -2.1 3.34 -3.92 2.49
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 2.01f,
          dx2 = -2.1f,
          dy2 = 3.34f,
          dx3 = -3.92f,
          dy3 = 2.49f,
        )
        // l -0.57 -0.27
        lineToRelative(dx = -0.57f, dy = -0.27f)
        // a 1.3 1.3 0 0 0 -1.04 -0.01
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.04f,
          dy1 = -0.01f,
        )
        // l -0.6 0.26
        lineToRelative(dx = -0.6f, dy = 0.26f)
        // a 2.8 2.8 0 0 1 -2.24 0
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.24f,
          dy1 = 0.0f,
        )
        // l -0.6 -0.26
        lineToRelative(dx = -0.6f, dy = -0.26f)
        // a 1.3 1.3 0 0 0 -1.04 0
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.04f,
          dy1 = 0.0f,
        )
        // l -0.57 0.28
        lineToRelative(dx = -0.57f, dy = 0.28f)
        // a 2.75 2.75 0 0 1 -3.92 -2.5z
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -3.92f,
          dy1 = -2.5f,
        )
        close()
        // M 7.5 3.75
        moveTo(x = 7.5f, y = 3.75f)
        // A 1.25 1.25 0 0 0 6.25 5
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 6.25f,
          y1 = 5.0f,
        )
        // v 13.85
        verticalLineToRelative(dy = 13.85f)
        // a 1.25 1.25 0 0 0 1.78 1.13
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.78f,
          dy1 = 1.13f,
        )
        // l 0.57 -0.27
        lineToRelative(dx = 0.57f, dy = -0.27f)
        // a 2.8 2.8 0 0 1 2.3 -0.02
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.3f,
          dy1 = -0.02f,
        )
        // l 0.6 0.26
        lineToRelative(dx = 0.6f, dy = 0.26f)
        // a 1.3 1.3 0 0 0 1 0
        arcToRelative(
          a = 1.3f,
          b = 1.3f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = 0.0f,
        )
        // l 0.6 -0.26
        lineToRelative(dx = 0.6f, dy = -0.26f)
        // a 2.8 2.8 0 0 1 2.3 0.02
        arcToRelative(
          a = 2.8f,
          b = 2.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.3f,
          dy1 = 0.02f,
        )
        // l 0.57 0.27
        lineToRelative(dx = 0.57f, dy = 0.27f)
        // a 1.25 1.25 0 0 0 1.78 -1.13
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.78f,
          dy1 = -1.13f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // a 1.25 1.25 0 0 0 -1.25 -1.25z
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.25f,
          dy1 = -1.25f,
        )
        close()
        // m 0.75 4.5
        moveToRelative(dx = 0.75f, dy = 4.5f)
        // A 0.75 0.75 0 0 1 9 7.5
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 9.0f,
          y1 = 7.5f,
        )
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // A 0.75 0.75 0 0 1 15 9
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 15.0f,
          y1 = 9.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // a 0.75 0.75 0 0 1 -0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = -0.75f,
        )
        // m 0 2.5
        moveToRelative(dx = 0.0f, dy = 2.5f)
        // A 0.75 0.75 0 0 1 9 10
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 9.0f,
          y1 = 10.0f,
        )
        // h 4
        horizontalLineToRelative(dx = 4.0f)
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
        // a 0.75 0.75 0 0 1 -0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = -0.75f,
        )
        // m 0 2.5
        moveToRelative(dx = 0.0f, dy = 2.5f)
        // A 0.75 0.75 0 0 1 9 12.5
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 9.0f,
          y1 = 12.5f,
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
        // H 9
        horizontalLineTo(x = 9.0f)
        // a 0.75 0.75 0 0 1 -0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = -0.75f,
        )
        // m 0 2.5
        moveToRelative(dx = 0.0f, dy = 2.5f)
        // A 0.75 0.75 0 0 1 9 15
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 9.0f,
          y1 = 15.0f,
        )
        // h 4
        horizontalLineToRelative(dx = 4.0f)
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
        // a 0.75 0.75 0 0 1 -0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -0.75f,
          dy1 = -0.75f,
        )
      }
    }.build().also { _paymentOutline = it }
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
        imageVector = HedvigIcons.PaymentOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _paymentOutline: ImageVector? = null
