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
val HedvigIcons.Clock: ImageVector
  get() {
    val current = _clock
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Clock",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 20 a8 8 0 1 0 0 -16 8 8 0 0 0 0 16 m0 1.5 a9.5 9.5 0 1 0 0 -19 9.5 9.5 0 0 0 0 19 m0 -15.25 A.75 .75 0 0 1 12.75 7 v4.69 l2.78 2.78 a.75 .75 0 0 1 -1.06 1.06 l-3 -3 A.8 .8 0 0 1 11.25 12 V7 A.75 .75 0 0 1 12 6.25
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 20
        moveTo(x = 12.0f, y = 20.0f)
        // a 8 8 0 1 0 0 -16
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -16.0f,
        )
        // a 8 8 0 0 0 0 16
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 16.0f,
        )
        // m 0 1.5
        moveToRelative(dx = 0.0f, dy = 1.5f)
        // a 9.5 9.5 0 1 0 0 -19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -19.0f,
        )
        // a 9.5 9.5 0 0 0 0 19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 19.0f,
        )
        // m 0 -15.25
        moveToRelative(dx = 0.0f, dy = -15.25f)
        // A 0.75 0.75 0 0 1 12.75 7
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.75f,
          y1 = 7.0f,
        )
        // v 4.69
        verticalLineToRelative(dy = 4.69f)
        // l 2.78 2.78
        lineToRelative(dx = 2.78f, dy = 2.78f)
        // a 0.75 0.75 0 0 1 -1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        // l -3 -3
        lineToRelative(dx = -3.0f, dy = -3.0f)
        // A 0.8 0.8 0 0 1 11.25 12
        arcTo(
          horizontalEllipseRadius = 0.8f,
          verticalEllipseRadius = 0.8f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 11.25f,
          y1 = 12.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // A 0.75 0.75 0 0 1 12 6.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 6.25f,
        )
      }
    }.build().also { _clock = it }
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
        imageVector = HedvigIcons.Clock,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _clock: ImageVector? = null
