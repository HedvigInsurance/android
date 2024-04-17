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
val HedvigIcons.Travel: ImageVector
  get() {
    val current = _travel
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Travel",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.25 4 A1.75 1.75 0 0 1 11 2.25 h2 A1.75 1.75 0 0 1 14.75 4 v1.25 h1.75 A2.75 2.75 0 0 1 19.25 8 v10 a2.75 2.75 0 0 1 -2.75 2.75 H16 a1 1 0 1 1 -2 0 h-4 a1 1 0 1 1 -2 0 H7.5 A2.75 2.75 0 0 1 4.75 18 V8 A2.75 2.75 0 0 1 7.5 5.25 h1.75z m4 0 v1.25 h-2.5 V4 A.25 .25 0 0 1 11 3.75 h2 A.25 .25 0 0 1 13.25 4 M7.5 6.75 A1.25 1.25 0 0 0 6.25 8 v10 a1.25 1.25 0 0 0 1.25 1.25 h9 A1.25 1.25 0 0 0 17.75 18 V8 a1.25 1.25 0 0 0 -1.25 -1.25z M11.25 9 a.75 .75 0 0 1 1.5 0 v8 a.75 .75 0 0 1 -1.5 0z M9 8.25 A.75 .75 0 0 0 8.25 9 v8 a.75 .75 0 0 0 1.5 0 V9 A.75 .75 0 0 0 9 8.25 M14.25 9 a.75 .75 0 0 1 1.5 0 v8 a.75 .75 0 0 1 -1.5 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 9.25 4
        moveTo(x = 9.25f, y = 4.0f)
        // A 1.75 1.75 0 0 1 11 2.25
        arcTo(
          horizontalEllipseRadius = 1.75f,
          verticalEllipseRadius = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 11.0f,
          y1 = 2.25f,
        )
        // h 2
        horizontalLineToRelative(dx = 2.0f)
        // A 1.75 1.75 0 0 1 14.75 4
        arcTo(
          horizontalEllipseRadius = 1.75f,
          verticalEllipseRadius = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 14.75f,
          y1 = 4.0f,
        )
        // v 1.25
        verticalLineToRelative(dy = 1.25f)
        // h 1.75
        horizontalLineToRelative(dx = 1.75f)
        // A 2.75 2.75 0 0 1 19.25 8
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.25f,
          y1 = 8.0f,
        )
        // v 10
        verticalLineToRelative(dy = 10.0f)
        // a 2.75 2.75 0 0 1 -2.75 2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.75f,
          dy1 = 2.75f,
        )
        // H 16
        horizontalLineTo(x = 16.0f)
        // a 1 1 0 1 1 -2 0
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -2.0f,
          dy1 = 0.0f,
        )
        // h -4
        horizontalLineToRelative(dx = -4.0f)
        // a 1 1 0 1 1 -2 0
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -2.0f,
          dy1 = 0.0f,
        )
        // H 7.5
        horizontalLineTo(x = 7.5f)
        // A 2.75 2.75 0 0 1 4.75 18
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 4.75f,
          y1 = 18.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // A 2.75 2.75 0 0 1 7.5 5.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.5f,
          y1 = 5.25f,
        )
        // h 1.75z
        horizontalLineToRelative(dx = 1.75f)
        close()
        // m 4 0
        moveToRelative(dx = 4.0f, dy = 0.0f)
        // v 1.25
        verticalLineToRelative(dy = 1.25f)
        // h -2.5
        horizontalLineToRelative(dx = -2.5f)
        // V 4
        verticalLineTo(y = 4.0f)
        // A 0.25 0.25 0 0 1 11 3.75
        arcTo(
          horizontalEllipseRadius = 0.25f,
          verticalEllipseRadius = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 11.0f,
          y1 = 3.75f,
        )
        // h 2
        horizontalLineToRelative(dx = 2.0f)
        // A 0.25 0.25 0 0 1 13.25 4
        arcTo(
          horizontalEllipseRadius = 0.25f,
          verticalEllipseRadius = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 13.25f,
          y1 = 4.0f,
        )
        // M 7.5 6.75
        moveTo(x = 7.5f, y = 6.75f)
        // A 1.25 1.25 0 0 0 6.25 8
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 6.25f,
          y1 = 8.0f,
        )
        // v 10
        verticalLineToRelative(dy = 10.0f)
        // a 1.25 1.25 0 0 0 1.25 1.25
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.25f,
          dy1 = 1.25f,
        )
        // h 9
        horizontalLineToRelative(dx = 9.0f)
        // A 1.25 1.25 0 0 0 17.75 18
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 17.75f,
          y1 = 18.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
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
        // M 11.25 9
        moveTo(x = 11.25f, y = 9.0f)
        // a 0.75 0.75 0 0 1 1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // a 0.75 0.75 0 0 1 -1.5 0z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        close()
        // M 9 8.25
        moveTo(x = 9.0f, y = 8.25f)
        // A 0.75 0.75 0 0 0 8.25 9
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 8.25f,
          y1 = 9.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // a 0.75 0.75 0 0 0 1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // V 9
        verticalLineTo(y = 9.0f)
        // A 0.75 0.75 0 0 0 9 8.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 9.0f,
          y1 = 8.25f,
        )
        // M 14.25 9
        moveTo(x = 14.25f, y = 9.0f)
        // a 0.75 0.75 0 0 1 1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // v 8
        verticalLineToRelative(dy = 8.0f)
        // a 0.75 0.75 0 0 1 -1.5 0z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        close()
      }
    }.build().also { _travel = it }
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
        imageVector = HedvigIcons.Travel,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _travel: ImageVector? = null
