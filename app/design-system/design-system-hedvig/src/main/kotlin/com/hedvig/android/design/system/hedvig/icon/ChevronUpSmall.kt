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
val HedvigIcons.ChevronUpSmall: ImageVector
  get() {
    val current = _chevronUpSmall
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronUpSmall",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M17.5303 14.5303 C17.2374 14.8232 16.7626 14.8232 16.4697 14.5303 L12.1768 10.2374 C12.0791 10.1398 11.9209 10.1398 11.8232 10.2374 L7.53033 14.5303 C7.23744 14.8232 6.76256 14.8232 6.46967 14.5303 C6.17678 14.2374 6.17678 13.7626 6.46967 13.4697 L10.7626 9.17678 C11.446 8.49336 12.554 8.49336 13.2374 9.17678 L17.5303 13.4697 C17.8232 13.7626 17.8232 14.2374 17.5303 14.5303Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 17.5303 14.5303
        moveTo(x = 17.5303f, y = 14.5303f)
        // C 17.2374 14.8232 16.7626 14.8232 16.4697 14.5303
        curveTo(
          x1 = 17.2374f,
          y1 = 14.8232f,
          x2 = 16.7626f,
          y2 = 14.8232f,
          x3 = 16.4697f,
          y3 = 14.5303f,
        )
        // L 12.1768 10.2374
        lineTo(x = 12.1768f, y = 10.2374f)
        // C 12.0791 10.1398 11.9209 10.1398 11.8232 10.2374
        curveTo(
          x1 = 12.0791f,
          y1 = 10.1398f,
          x2 = 11.9209f,
          y2 = 10.1398f,
          x3 = 11.8232f,
          y3 = 10.2374f,
        )
        // L 7.53033 14.5303
        lineTo(x = 7.53033f, y = 14.5303f)
        // C 7.23744 14.8232 6.76256 14.8232 6.46967 14.5303
        curveTo(
          x1 = 7.23744f,
          y1 = 14.8232f,
          x2 = 6.76256f,
          y2 = 14.8232f,
          x3 = 6.46967f,
          y3 = 14.5303f,
        )
        // C 6.17678 14.2374 6.17678 13.7626 6.46967 13.4697
        curveTo(
          x1 = 6.17678f,
          y1 = 14.2374f,
          x2 = 6.17678f,
          y2 = 13.7626f,
          x3 = 6.46967f,
          y3 = 13.4697f,
        )
        // L 10.7626 9.17678
        lineTo(x = 10.7626f, y = 9.17678f)
        // C 11.446 8.49336 12.554 8.49336 13.2374 9.17678
        curveTo(
          x1 = 11.446f,
          y1 = 8.49336f,
          x2 = 12.554f,
          y2 = 8.49336f,
          x3 = 13.2374f,
          y3 = 9.17678f,
        )
        // L 17.5303 13.4697
        lineTo(x = 17.5303f, y = 13.4697f)
        // C 17.8232 13.7626 17.8232 14.2374 17.5303 14.5303z
        curveTo(
          x1 = 17.8232f,
          y1 = 13.7626f,
          x2 = 17.8232f,
          y2 = 14.2374f,
          x3 = 17.5303f,
          y3 = 14.5303f,
        )
        close()
      }
    }.build().also { _chevronUpSmall = it }
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
        imageVector = HedvigIcons.ChevronUpSmall,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronUpSmall: ImageVector? = null
