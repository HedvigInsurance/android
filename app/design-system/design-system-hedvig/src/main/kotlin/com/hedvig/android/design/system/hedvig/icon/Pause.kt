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

val Pause: ImageVector
  get() {
    val current = _pause
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Pause",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M8 6 a1 1 0 0 0 -1 1 v10 a1 1 0 0 0 1 1 h1 a1 1 0 0 0 1 -1 V7 a1 1 0 0 0 -1 -1z m7 0 a1 1 0 0 0 -1 1 v10 a1 1 0 0 0 1 1 h1 a1 1 0 0 0 1 -1 V7 a1 1 0 0 0 -1 -1z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 8 6
        moveTo(x = 8.0f, y = 6.0f)
        // a 1 1 0 0 0 -1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = 1.0f,
        )
        // v 10
        verticalLineToRelative(dy = 10.0f)
        // a 1 1 0 0 0 1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = 1.0f,
        )
        // h 1
        horizontalLineToRelative(dx = 1.0f)
        // a 1 1 0 0 0 1 -1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = -1.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // a 1 1 0 0 0 -1 -1z
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = -1.0f,
        )
        close()
        // m 7 0
        moveToRelative(dx = 7.0f, dy = 0.0f)
        // a 1 1 0 0 0 -1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = 1.0f,
        )
        // v 10
        verticalLineToRelative(dy = 10.0f)
        // a 1 1 0 0 0 1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = 1.0f,
        )
        // h 1
        horizontalLineToRelative(dx = 1.0f)
        // a 1 1 0 0 0 1 -1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = -1.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // a 1 1 0 0 0 -1 -1z
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = -1.0f,
        )
        close()
      }
    }.build().also { _pause = it }
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
        imageVector = Pause,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _pause: ImageVector? = null
