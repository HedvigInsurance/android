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
val HedvigIcons.ChevronDownSmall: ImageVector
  get() {
    val current = _chevronDownSmall
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronDownSmall",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.46967 9.96967 C6.76256 9.67678 7.23744 9.67678 7.53033 9.96967 L11.8232 14.2626 C11.9209 14.3602 12.0791 14.3602 12.1768 14.2626 L16.4697 9.96967 C16.7626 9.67678 17.2374 9.67678 17.5303 9.96967 C17.8232 10.2626 17.8232 10.7374 17.5303 11.0303 L13.2374 15.3232 C12.554 16.0066 11.446 16.0066 10.7626 15.3232 L6.46967 11.0303 C6.17678 10.7374 6.17678 10.2626 6.46967 9.96967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 6.46967 9.96967
        moveTo(x = 6.46967f, y = 9.96967f)
        // C 6.76256 9.67678 7.23744 9.67678 7.53033 9.96967
        curveTo(
          x1 = 6.76256f,
          y1 = 9.67678f,
          x2 = 7.23744f,
          y2 = 9.67678f,
          x3 = 7.53033f,
          y3 = 9.96967f,
        )
        // L 11.8232 14.2626
        lineTo(x = 11.8232f, y = 14.2626f)
        // C 11.9209 14.3602 12.0791 14.3602 12.1768 14.2626
        curveTo(
          x1 = 11.9209f,
          y1 = 14.3602f,
          x2 = 12.0791f,
          y2 = 14.3602f,
          x3 = 12.1768f,
          y3 = 14.2626f,
        )
        // L 16.4697 9.96967
        lineTo(x = 16.4697f, y = 9.96967f)
        // C 16.7626 9.67678 17.2374 9.67678 17.5303 9.96967
        curveTo(
          x1 = 16.7626f,
          y1 = 9.67678f,
          x2 = 17.2374f,
          y2 = 9.67678f,
          x3 = 17.5303f,
          y3 = 9.96967f,
        )
        // C 17.8232 10.2626 17.8232 10.7374 17.5303 11.0303
        curveTo(
          x1 = 17.8232f,
          y1 = 10.2626f,
          x2 = 17.8232f,
          y2 = 10.7374f,
          x3 = 17.5303f,
          y3 = 11.0303f,
        )
        // L 13.2374 15.3232
        lineTo(x = 13.2374f, y = 15.3232f)
        // C 12.554 16.0066 11.446 16.0066 10.7626 15.3232
        curveTo(
          x1 = 12.554f,
          y1 = 16.0066f,
          x2 = 11.446f,
          y2 = 16.0066f,
          x3 = 10.7626f,
          y3 = 15.3232f,
        )
        // L 6.46967 11.0303
        lineTo(x = 6.46967f, y = 11.0303f)
        // C 6.17678 10.7374 6.17678 10.2626 6.46967 9.96967z
        curveTo(
          x1 = 6.17678f,
          y1 = 10.7374f,
          x2 = 6.17678f,
          y2 = 10.2626f,
          x3 = 6.46967f,
          y3 = 9.96967f,
        )
        close()
      }
    }.build().also { _chevronDownSmall = it }
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
        imageVector = HedvigIcons.ChevronDownSmall,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronDownSmall: ImageVector? = null
