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
val HedvigIcons.ChevronLeftIOS: ImageVector
  get() {
    val current = _chevronLeftIOS
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronLeftIOS",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.5303 4.96967 C10.8232 5.26256 10.8232 5.73744 10.5303 6.03033 L4.73744 11.8232 C4.63981 11.9209 4.63981 12.0791 4.73744 12.1768 L10.5303 17.9697 C10.8232 18.2626 10.8232 18.7374 10.5303 19.0303 C10.2374 19.3232 9.76256 19.3232 9.46967 19.0303 L3.67678 13.2374 C2.99336 12.554 2.99336 11.446 3.67678 10.7626 L9.46967 4.96967 C9.76256 4.67678 10.2374 4.67678 10.5303 4.96967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 10.5303 4.96967
        moveTo(x = 10.5303f, y = 4.96967f)
        // C 10.8232 5.26256 10.8232 5.73744 10.5303 6.03033
        curveTo(
          x1 = 10.8232f,
          y1 = 5.26256f,
          x2 = 10.8232f,
          y2 = 5.73744f,
          x3 = 10.5303f,
          y3 = 6.03033f,
        )
        // L 4.73744 11.8232
        lineTo(x = 4.73744f, y = 11.8232f)
        // C 4.63981 11.9209 4.63981 12.0791 4.73744 12.1768
        curveTo(
          x1 = 4.63981f,
          y1 = 11.9209f,
          x2 = 4.63981f,
          y2 = 12.0791f,
          x3 = 4.73744f,
          y3 = 12.1768f,
        )
        // L 10.5303 17.9697
        lineTo(x = 10.5303f, y = 17.9697f)
        // C 10.8232 18.2626 10.8232 18.7374 10.5303 19.0303
        curveTo(
          x1 = 10.8232f,
          y1 = 18.2626f,
          x2 = 10.8232f,
          y2 = 18.7374f,
          x3 = 10.5303f,
          y3 = 19.0303f,
        )
        // C 10.2374 19.3232 9.76256 19.3232 9.46967 19.0303
        curveTo(
          x1 = 10.2374f,
          y1 = 19.3232f,
          x2 = 9.76256f,
          y2 = 19.3232f,
          x3 = 9.46967f,
          y3 = 19.0303f,
        )
        // L 3.67678 13.2374
        lineTo(x = 3.67678f, y = 13.2374f)
        // C 2.99336 12.554 2.99336 11.446 3.67678 10.7626
        curveTo(
          x1 = 2.99336f,
          y1 = 12.554f,
          x2 = 2.99336f,
          y2 = 11.446f,
          x3 = 3.67678f,
          y3 = 10.7626f,
        )
        // L 9.46967 4.96967
        lineTo(x = 9.46967f, y = 4.96967f)
        // C 9.76256 4.67678 10.2374 4.67678 10.5303 4.96967z
        curveTo(
          x1 = 9.76256f,
          y1 = 4.67678f,
          x2 = 10.2374f,
          y2 = 4.67678f,
          x3 = 10.5303f,
          y3 = 4.96967f,
        )
        close()
      }
    }.build().also { _chevronLeftIOS = it }
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
        imageVector = HedvigIcons.ChevronLeftIOS,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronLeftIOS: ImageVector? = null
