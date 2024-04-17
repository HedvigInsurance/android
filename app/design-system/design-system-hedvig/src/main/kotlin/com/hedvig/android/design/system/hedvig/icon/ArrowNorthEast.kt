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
val HedvigIcons.ArrowNorthEast: ImageVector
  get() {
    val current = _arrowNorthEast
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ArrowNorthEast",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10 5.25 a.75 .75 0 0 0 0 1.5 h6.19 L5.1 17.83 a.75 .75 0 1 0 1.06 1.06 L17.25 7.81 V14 a.75 .75 0 0 0 1.5 0 V7 A1.75 1.75 0 0 0 17 5.25z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 10 5.25
        moveTo(x = 10.0f, y = 5.25f)
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
        // h 6.19
        horizontalLineToRelative(dx = 6.19f)
        // L 5.1 17.83
        lineTo(x = 5.1f, y = 17.83f)
        // a 0.75 0.75 0 1 0 1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = 1.06f,
        )
        // L 17.25 7.81
        lineTo(x = 17.25f, y = 7.81f)
        // V 14
        verticalLineTo(y = 14.0f)
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
        // V 7
        verticalLineTo(y = 7.0f)
        // A 1.75 1.75 0 0 0 17 5.25z
        arcTo(
          horizontalEllipseRadius = 1.75f,
          verticalEllipseRadius = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 17.0f,
          y1 = 5.25f,
        )
        close()
      }
    }.build().also { _arrowNorthEast = it }
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
        imageVector = ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowNorthEast: ImageVector? = null
