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
val HedvigIcons.Pause: ImageVector
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
      // M8 6 C7.44772 6 7 6.44771 7 7 V17 C7 17.5523 7.44772 18 8 18 H9 C9.55228 18 10 17.5523 10 17 V7 C10 6.44772 9.55228 6 9 6 H8Z M15 6 C14.4477 6 14 6.44771 14 7 V17 C14 17.5523 14.4477 18 15 18 H16 C16.5523 18 17 17.5523 17 17 V7 C17 6.44772 16.5523 6 16 6 H15Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 8 6
        moveTo(x = 8.0f, y = 6.0f)
        // C 7.44772 6 7 6.44771 7 7
        curveTo(
          x1 = 7.44772f,
          y1 = 6.0f,
          x2 = 7.0f,
          y2 = 6.44771f,
          x3 = 7.0f,
          y3 = 7.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 7 17.5523 7.44772 18 8 18
        curveTo(
          x1 = 7.0f,
          y1 = 17.5523f,
          x2 = 7.44772f,
          y2 = 18.0f,
          x3 = 8.0f,
          y3 = 18.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 9.55228 18 10 17.5523 10 17
        curveTo(
          x1 = 9.55228f,
          y1 = 18.0f,
          x2 = 10.0f,
          y2 = 17.5523f,
          x3 = 10.0f,
          y3 = 17.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 10 6.44772 9.55228 6 9 6
        curveTo(
          x1 = 10.0f,
          y1 = 6.44772f,
          x2 = 9.55228f,
          y2 = 6.0f,
          x3 = 9.0f,
          y3 = 6.0f,
        )
        // H 8z
        horizontalLineTo(x = 8.0f)
        close()
        // M 15 6
        moveTo(x = 15.0f, y = 6.0f)
        // C 14.4477 6 14 6.44771 14 7
        curveTo(
          x1 = 14.4477f,
          y1 = 6.0f,
          x2 = 14.0f,
          y2 = 6.44771f,
          x3 = 14.0f,
          y3 = 7.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 14 17.5523 14.4477 18 15 18
        curveTo(
          x1 = 14.0f,
          y1 = 17.5523f,
          x2 = 14.4477f,
          y2 = 18.0f,
          x3 = 15.0f,
          y3 = 18.0f,
        )
        // H 16
        horizontalLineTo(x = 16.0f)
        // C 16.5523 18 17 17.5523 17 17
        curveTo(
          x1 = 16.5523f,
          y1 = 18.0f,
          x2 = 17.0f,
          y2 = 17.5523f,
          x3 = 17.0f,
          y3 = 17.0f,
        )
        // V 7
        verticalLineTo(y = 7.0f)
        // C 17 6.44772 16.5523 6 16 6
        curveTo(
          x1 = 17.0f,
          y1 = 6.44772f,
          x2 = 16.5523f,
          y2 = 6.0f,
          x3 = 16.0f,
          y3 = 6.0f,
        )
        // H 15z
        horizontalLineTo(x = 15.0f)
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
        imageVector = HedvigIcons.Pause,
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
