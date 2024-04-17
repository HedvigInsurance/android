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
val HedvigIcons.Download: ImageVector
  get() {
    val current = _download
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Download",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // m12.75 13.19 2.22 -2.22 a.75 .75 0 1 1 1.06 1.06 l-2.8 2.8 a1.75 1.75 0 0 1 -2.47 0 l-2.79 -2.8 a.75 .75 0 1 1 1.06 -1.06 l2.22 2.22 V5.5 a.75 .75 0 0 1 1.5 0z M5 14.75 a.75 .75 0 0 1 .75 .75 v2 A.25 .25 0 0 0 6 17.75 h12 a.25 .25 0 0 0 .25 -.25 v-2 a.75 .75 0 0 1 1.5 0 v2 A1.75 1.75 0 0 1 18 19.25 H6 a1.75 1.75 0 0 1 -1.75 -1.75 v-2 A.75 .75 0 0 1 5 14.75
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // m 12.75 13.19
        moveToRelative(dx = 12.75f, dy = 13.19f)
        // l 2.22 -2.22
        lineToRelative(dx = 2.22f, dy = -2.22f)
        // a 0.75 0.75 0 1 1 1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = 1.06f,
        )
        // l -2.8 2.8
        lineToRelative(dx = -2.8f, dy = 2.8f)
        // a 1.75 1.75 0 0 1 -2.47 0
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.47f,
          dy1 = 0.0f,
        )
        // l -2.79 -2.8
        lineToRelative(dx = -2.79f, dy = -2.8f)
        // a 0.75 0.75 0 1 1 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l 2.22 2.22
        lineToRelative(dx = 2.22f, dy = 2.22f)
        // V 5.5
        verticalLineTo(y = 5.5f)
        // a 0.75 0.75 0 0 1 1.5 0z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        close()
        // M 5 14.75
        moveTo(x = 5.0f, y = 14.75f)
        // a 0.75 0.75 0 0 1 0.75 0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.75f,
          dy1 = 0.75f,
        )
        // v 2
        verticalLineToRelative(dy = 2.0f)
        // A 0.25 0.25 0 0 0 6 17.75
        arcTo(
          horizontalEllipseRadius = 0.25f,
          verticalEllipseRadius = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 6.0f,
          y1 = 17.75f,
        )
        // h 12
        horizontalLineToRelative(dx = 12.0f)
        // a 0.25 0.25 0 0 0 0.25 -0.25
        arcToRelative(
          a = 0.25f,
          b = 0.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.25f,
          dy1 = -0.25f,
        )
        // v -2
        verticalLineToRelative(dy = -2.0f)
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
        // v 2
        verticalLineToRelative(dy = 2.0f)
        // A 1.75 1.75 0 0 1 18 19.25
        arcTo(
          horizontalEllipseRadius = 1.75f,
          verticalEllipseRadius = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 18.0f,
          y1 = 19.25f,
        )
        // H 6
        horizontalLineTo(x = 6.0f)
        // a 1.75 1.75 0 0 1 -1.75 -1.75
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.75f,
          dy1 = -1.75f,
        )
        // v -2
        verticalLineToRelative(dy = -2.0f)
        // A 0.75 0.75 0 0 1 5 14.75
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 5.0f,
          y1 = 14.75f,
        )
      }
    }.build().also { _download = it }
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
        imageVector = Download,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _download: ImageVector? = null
