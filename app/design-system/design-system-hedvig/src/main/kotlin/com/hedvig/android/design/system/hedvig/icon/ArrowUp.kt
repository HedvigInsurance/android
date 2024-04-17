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
val HedvigIcons.ArrowUp: ImageVector
  get() {
    val current = _arrowUp
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ArrowUp",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M17.9697 12.5302 C18.2626 12.8231 18.7374 12.8231 19.0303 12.5302 C19.3232 12.2373 19.3232 11.7624 19.0303 11.4695 L13.2374 5.67663 C12.554 4.99321 11.446 4.99321 10.7626 5.67662 L4.96967 11.4695 C4.67678 11.7624 4.67678 12.2373 4.96967 12.5302 C5.26256 12.8231 5.73744 12.8231 6.03033 12.5302 L11.25 7.31051 L11.25 18.9999 C11.25 19.4141 11.5858 19.7499 12 19.7499 C12.4142 19.7499 12.75 19.4141 12.75 18.9999 L12.75 7.31051 L17.9697 12.5302Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 17.9697 12.5302
        moveTo(x = 17.9697f, y = 12.5302f)
        // C 18.2626 12.8231 18.7374 12.8231 19.0303 12.5302
        curveTo(
          x1 = 18.2626f,
          y1 = 12.8231f,
          x2 = 18.7374f,
          y2 = 12.8231f,
          x3 = 19.0303f,
          y3 = 12.5302f,
        )
        // C 19.3232 12.2373 19.3232 11.7624 19.0303 11.4695
        curveTo(
          x1 = 19.3232f,
          y1 = 12.2373f,
          x2 = 19.3232f,
          y2 = 11.7624f,
          x3 = 19.0303f,
          y3 = 11.4695f,
        )
        // L 13.2374 5.67663
        lineTo(x = 13.2374f, y = 5.67663f)
        // C 12.554 4.99321 11.446 4.99321 10.7626 5.67662
        curveTo(
          x1 = 12.554f,
          y1 = 4.99321f,
          x2 = 11.446f,
          y2 = 4.99321f,
          x3 = 10.7626f,
          y3 = 5.67662f,
        )
        // L 4.96967 11.4695
        lineTo(x = 4.96967f, y = 11.4695f)
        // C 4.67678 11.7624 4.67678 12.2373 4.96967 12.5302
        curveTo(
          x1 = 4.67678f,
          y1 = 11.7624f,
          x2 = 4.67678f,
          y2 = 12.2373f,
          x3 = 4.96967f,
          y3 = 12.5302f,
        )
        // C 5.26256 12.8231 5.73744 12.8231 6.03033 12.5302
        curveTo(
          x1 = 5.26256f,
          y1 = 12.8231f,
          x2 = 5.73744f,
          y2 = 12.8231f,
          x3 = 6.03033f,
          y3 = 12.5302f,
        )
        // L 11.25 7.31051
        lineTo(x = 11.25f, y = 7.31051f)
        // L 11.25 18.9999
        lineTo(x = 11.25f, y = 18.9999f)
        // C 11.25 19.4141 11.5858 19.7499 12 19.7499
        curveTo(
          x1 = 11.25f,
          y1 = 19.4141f,
          x2 = 11.5858f,
          y2 = 19.7499f,
          x3 = 12.0f,
          y3 = 19.7499f,
        )
        // C 12.4142 19.7499 12.75 19.4141 12.75 18.9999
        curveTo(
          x1 = 12.4142f,
          y1 = 19.7499f,
          x2 = 12.75f,
          y2 = 19.4141f,
          x3 = 12.75f,
          y3 = 18.9999f,
        )
        // L 12.75 7.31051
        lineTo(x = 12.75f, y = 7.31051f)
        // L 17.9697 12.5302z
        lineTo(x = 17.9697f, y = 12.5302f)
        close()
      }
    }.build().also { _arrowUp = it }
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
        imageVector = HedvigIcons.ArrowUp,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowUp: ImageVector? = null
