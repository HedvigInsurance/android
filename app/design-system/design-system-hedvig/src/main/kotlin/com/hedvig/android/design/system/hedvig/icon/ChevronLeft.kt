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
val HedvigIcons.ChevronLeft: ImageVector
  get() {
    val current = _chevronLeft
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ChevronLeft",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M14.5303 4.96967 C14.8232 5.26256 14.8232 5.73744 14.5303 6.03033 L8.73744 11.8232 C8.63981 11.9209 8.63981 12.0791 8.73744 12.1768 L14.5303 17.9697 C14.8232 18.2626 14.8232 18.7374 14.5303 19.0303 C14.2374 19.3232 13.7626 19.3232 13.4697 19.0303 L7.67678 13.2374 C6.99336 12.554 6.99336 11.446 7.67678 10.7626 L13.4697 4.96967 C13.7626 4.67678 14.2374 4.67678 14.5303 4.96967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 14.5303 4.96967
        moveTo(x = 14.5303f, y = 4.96967f)
        // C 14.8232 5.26256 14.8232 5.73744 14.5303 6.03033
        curveTo(
          x1 = 14.8232f,
          y1 = 5.26256f,
          x2 = 14.8232f,
          y2 = 5.73744f,
          x3 = 14.5303f,
          y3 = 6.03033f,
        )
        // L 8.73744 11.8232
        lineTo(x = 8.73744f, y = 11.8232f)
        // C 8.63981 11.9209 8.63981 12.0791 8.73744 12.1768
        curveTo(
          x1 = 8.63981f,
          y1 = 11.9209f,
          x2 = 8.63981f,
          y2 = 12.0791f,
          x3 = 8.73744f,
          y3 = 12.1768f,
        )
        // L 14.5303 17.9697
        lineTo(x = 14.5303f, y = 17.9697f)
        // C 14.8232 18.2626 14.8232 18.7374 14.5303 19.0303
        curveTo(
          x1 = 14.8232f,
          y1 = 18.2626f,
          x2 = 14.8232f,
          y2 = 18.7374f,
          x3 = 14.5303f,
          y3 = 19.0303f,
        )
        // C 14.2374 19.3232 13.7626 19.3232 13.4697 19.0303
        curveTo(
          x1 = 14.2374f,
          y1 = 19.3232f,
          x2 = 13.7626f,
          y2 = 19.3232f,
          x3 = 13.4697f,
          y3 = 19.0303f,
        )
        // L 7.67678 13.2374
        lineTo(x = 7.67678f, y = 13.2374f)
        // C 6.99336 12.554 6.99336 11.446 7.67678 10.7626
        curveTo(
          x1 = 6.99336f,
          y1 = 12.554f,
          x2 = 6.99336f,
          y2 = 11.446f,
          x3 = 7.67678f,
          y3 = 10.7626f,
        )
        // L 13.4697 4.96967
        lineTo(x = 13.4697f, y = 4.96967f)
        // C 13.7626 4.67678 14.2374 4.67678 14.5303 4.96967z
        curveTo(
          x1 = 13.7626f,
          y1 = 4.67678f,
          x2 = 14.2374f,
          y2 = 4.67678f,
          x3 = 14.5303f,
          y3 = 4.96967f,
        )
        close()
      }
    }.build().also { _chevronLeft = it }
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
        imageVector = HedvigIcons.ChevronLeft,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronLeft: ImageVector? = null
