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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ChevronLeftSmall: ImageVector
  get() {
    val current = _chevronLeftSmall
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ChevronLeftSmall",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M14.0303 6.46967 C14.3232 6.76256 14.3232 7.23744 14.0303 7.53033 L9.73744 11.8232 C9.63981 11.9209 9.63981 12.0791 9.73744 12.1768 L14.0303 16.4697 C14.3232 16.7626 14.3232 17.2374 14.0303 17.5303 C13.7374 17.8232 13.2626 17.8232 12.9697 17.5303 L8.67678 13.2374 C7.99336 12.554 7.99336 11.446 8.67678 10.7626 L12.9697 6.46967 C13.2626 6.17678 13.7374 6.17678 14.0303 6.46967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 14.0303 6.46967
        moveTo(x = 14.0303f, y = 6.46967f)
        // C 14.3232 6.76256 14.3232 7.23744 14.0303 7.53033
        curveTo(
          x1 = 14.3232f,
          y1 = 6.76256f,
          x2 = 14.3232f,
          y2 = 7.23744f,
          x3 = 14.0303f,
          y3 = 7.53033f,
        )
        // L 9.73744 11.8232
        lineTo(x = 9.73744f, y = 11.8232f)
        // C 9.63981 11.9209 9.63981 12.0791 9.73744 12.1768
        curveTo(
          x1 = 9.63981f,
          y1 = 11.9209f,
          x2 = 9.63981f,
          y2 = 12.0791f,
          x3 = 9.73744f,
          y3 = 12.1768f,
        )
        // L 14.0303 16.4697
        lineTo(x = 14.0303f, y = 16.4697f)
        // C 14.3232 16.7626 14.3232 17.2374 14.0303 17.5303
        curveTo(
          x1 = 14.3232f,
          y1 = 16.7626f,
          x2 = 14.3232f,
          y2 = 17.2374f,
          x3 = 14.0303f,
          y3 = 17.5303f,
        )
        // C 13.7374 17.8232 13.2626 17.8232 12.9697 17.5303
        curveTo(
          x1 = 13.7374f,
          y1 = 17.8232f,
          x2 = 13.2626f,
          y2 = 17.8232f,
          x3 = 12.9697f,
          y3 = 17.5303f,
        )
        // L 8.67678 13.2374
        lineTo(x = 8.67678f, y = 13.2374f)
        // C 7.99336 12.554 7.99336 11.446 8.67678 10.7626
        curveTo(
          x1 = 7.99336f,
          y1 = 12.554f,
          x2 = 7.99336f,
          y2 = 11.446f,
          x3 = 8.67678f,
          y3 = 10.7626f,
        )
        // L 12.9697 6.46967
        lineTo(x = 12.9697f, y = 6.46967f)
        // C 13.2626 6.17678 13.7374 6.17678 14.0303 6.46967z
        curveTo(
          x1 = 13.2626f,
          y1 = 6.17678f,
          x2 = 13.7374f,
          y2 = 6.17678f,
          x3 = 14.0303f,
          y3 = 6.46967f,
        )
        close()
      }
    }.build().also { _chevronLeftSmall = it }
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
        imageVector = HedvigIcons.ChevronLeftSmall,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronLeftSmall: ImageVector? = null
