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
val HedvigIcons.Copy: ImageVector
  get() {
    val current = _copy
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Copy",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.89 1.75 A2.75 2.75 0 0 0 8.14 4.5 v.7 H7 a2.75 2.75 0 0 0 -2.75 2.75 V19.5 A2.75 2.75 0 0 0 7 22.25 h6.11 a2.75 2.75 0 0 0 2.75 -2.75 v-.7 H17 a2.75 2.75 0 0 0 2.75 -2.75 V4.5 A2.75 2.75 0 0 0 17 1.75z m4.97 15.55 H17 a1.25 1.25 0 0 0 1.25 -1.25 V4.5 A1.25 1.25 0 0 0 17 3.25 h-6.11 A1.25 1.25 0 0 0 9.64 4.5 v.7 h3.47 a2.75 2.75 0 0 1 2.75 2.75z M5.75 7.95 A1.25 1.25 0 0 1 7 6.7 h6.11 a1.25 1.25 0 0 1 1.25 1.25 V19.5 a1.25 1.25 0 0 1 -1.25 1.25 H7 a1.25 1.25 0 0 1 -1.25 -1.25z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 10.89 1.75
        moveTo(x = 10.89f, y = 1.75f)
        // A 2.75 2.75 0 0 0 8.14 4.5
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 8.14f,
          y1 = 4.5f,
        )
        // v 0.7
        verticalLineToRelative(dy = 0.7f)
        // H 7
        horizontalLineTo(x = 7.0f)
        // a 2.75 2.75 0 0 0 -2.75 2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -2.75f,
          dy1 = 2.75f,
        )
        // V 19.5
        verticalLineTo(y = 19.5f)
        // A 2.75 2.75 0 0 0 7 22.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 7.0f,
          y1 = 22.25f,
        )
        // h 6.11
        horizontalLineToRelative(dx = 6.11f)
        // a 2.75 2.75 0 0 0 2.75 -2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.75f,
          dy1 = -2.75f,
        )
        // v -0.7
        verticalLineToRelative(dy = -0.7f)
        // H 17
        horizontalLineTo(x = 17.0f)
        // a 2.75 2.75 0 0 0 2.75 -2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 2.75f,
          dy1 = -2.75f,
        )
        // V 4.5
        verticalLineTo(y = 4.5f)
        // A 2.75 2.75 0 0 0 17 1.75z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 17.0f,
          y1 = 1.75f,
        )
        close()
        // m 4.97 15.55
        moveToRelative(dx = 4.97f, dy = 15.55f)
        // H 17
        horizontalLineTo(x = 17.0f)
        // a 1.25 1.25 0 0 0 1.25 -1.25
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.25f,
          dy1 = -1.25f,
        )
        // V 4.5
        verticalLineTo(y = 4.5f)
        // A 1.25 1.25 0 0 0 17 3.25
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 17.0f,
          y1 = 3.25f,
        )
        // h -6.11
        horizontalLineToRelative(dx = -6.11f)
        // A 1.25 1.25 0 0 0 9.64 4.5
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 9.64f,
          y1 = 4.5f,
        )
        // v 0.7
        verticalLineToRelative(dy = 0.7f)
        // h 3.47
        horizontalLineToRelative(dx = 3.47f)
        // a 2.75 2.75 0 0 1 2.75 2.75z
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.75f,
          dy1 = 2.75f,
        )
        close()
        // M 5.75 7.95
        moveTo(x = 5.75f, y = 7.95f)
        // A 1.25 1.25 0 0 1 7 6.7
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.0f,
          y1 = 6.7f,
        )
        // h 6.11
        horizontalLineToRelative(dx = 6.11f)
        // a 1.25 1.25 0 0 1 1.25 1.25
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.25f,
          dy1 = 1.25f,
        )
        // V 19.5
        verticalLineTo(y = 19.5f)
        // a 1.25 1.25 0 0 1 -1.25 1.25
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.25f,
          dy1 = 1.25f,
        )
        // H 7
        horizontalLineTo(x = 7.0f)
        // a 1.25 1.25 0 0 1 -1.25 -1.25z
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.25f,
          dy1 = -1.25f,
        )
        close()
      }
    }.build().also { _copy = it }
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
        imageVector = HedvigIcons.Copy,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _copy: ImageVector? = null
