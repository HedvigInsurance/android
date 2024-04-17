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
val HedvigIcons.Swap: ImageVector
  get() {
    val current = _swap
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Swap",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M11.97 7.53 9.75 5.31 V13 a.75 .75 0 0 1 -1.5 0 V5.31 L6.03 7.53 a.75 .75 0 0 1 -1.06 -1.06 l2.8 -2.8 a1.75 1.75 0 0 1 2.47 0 l2.79 2.8 a.75 .75 0 0 1 -1.06 1.06 m3.78 11.16 2.22 -2.22 a.75 .75 0 1 1 1.06 1.06 l-2.8 2.8 a1.75 1.75 0 0 1 -2.47 0 l-2.79 -2.8 a.75 .75 0 1 1 1.06 -1.06 l2.22 2.22 V11 a.75 .75 0 0 1 1.5 0z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 11.97 7.53
        moveTo(x = 11.97f, y = 7.53f)
        // L 9.75 5.31
        lineTo(x = 9.75f, y = 5.31f)
        // V 13
        verticalLineTo(y = 13.0f)
        // a 0.75 0.75 0 0 1 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // V 5.31
        verticalLineTo(y = 5.31f)
        // L 6.03 7.53
        lineTo(x = 6.03f, y = 7.53f)
        // a 0.75 0.75 0 0 1 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l 2.8 -2.8
        lineToRelative(dx = 2.8f, dy = -2.8f)
        // a 1.75 1.75 0 0 1 2.47 0
        arcToRelative(
          a = 1.75f,
          b = 1.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.47f,
          dy1 = 0.0f,
        )
        // l 2.79 2.8
        lineToRelative(dx = 2.79f, dy = 2.8f)
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
        // m 3.78 11.16
        moveToRelative(dx = 3.78f, dy = 11.16f)
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
        // V 11
        verticalLineTo(y = 11.0f)
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
      }
    }.build().also { _swap = it }
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
        imageVector = HedvigIcons.Swap,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _swap: ImageVector? = null
