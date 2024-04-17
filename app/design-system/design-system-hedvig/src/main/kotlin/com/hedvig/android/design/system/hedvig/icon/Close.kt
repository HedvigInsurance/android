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
val HedvigIcons.Close: ImageVector
  get() {
    val current = _close
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Close",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.87 5.81 a.75 .75 0 0 0 -1.06 1.06 L10.94 12 5.8 17.13 a.75 .75 0 1 0 1.06 1.06 L12 13.06 l5.13 5.13 a.75 .75 0 0 0 1.06 -1.06 L13.06 12 l5.13 -5.13 a.75 .75 0 0 0 -1.06 -1.06 L12 10.94z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 6.87 5.81
        moveTo(x = 6.87f, y = 5.81f)
        // a 0.75 0.75 0 0 0 -1.06 1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        // L 10.94 12
        lineTo(x = 10.94f, y = 12.0f)
        // L 5.8 17.13
        lineTo(x = 5.8f, y = 17.13f)
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
        // L 12 13.06
        lineTo(x = 12.0f, y = 13.06f)
        // l 5.13 5.13
        lineToRelative(dx = 5.13f, dy = 5.13f)
        // a 0.75 0.75 0 0 0 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // L 13.06 12
        lineTo(x = 13.06f, y = 12.0f)
        // l 5.13 -5.13
        lineToRelative(dx = 5.13f, dy = -5.13f)
        // a 0.75 0.75 0 0 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // L 12 10.94z
        lineTo(x = 12.0f, y = 10.94f)
        close()
      }
    }.build().also { _close = it }
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
        imageVector = HedvigIcons.Close,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _close: ImageVector? = null
