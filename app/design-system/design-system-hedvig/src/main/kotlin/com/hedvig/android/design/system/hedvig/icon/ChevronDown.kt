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
val HedvigIcons.ChevronDown: ImageVector
  get() {
    val current = _chevronDown
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ChevronDown",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M4.96967 8.96967 C5.26256 8.67678 5.73744 8.67678 6.03033 8.96967 L11.8232 14.7626 C11.9209 14.8602 12.0791 14.8602 12.1768 14.7626 L17.9697 8.96967 C18.2626 8.67678 18.7374 8.67678 19.0303 8.96967 C19.3232 9.26256 19.3232 9.73744 19.0303 10.0303 L13.2374 15.8232 C12.554 16.5066 11.446 16.5066 10.7626 15.8232 L4.96967 10.0303 C4.67678 9.73744 4.67678 9.26256 4.96967 8.96967Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 4.96967 8.96967
        moveTo(x = 4.96967f, y = 8.96967f)
        // C 5.26256 8.67678 5.73744 8.67678 6.03033 8.96967
        curveTo(
          x1 = 5.26256f,
          y1 = 8.67678f,
          x2 = 5.73744f,
          y2 = 8.67678f,
          x3 = 6.03033f,
          y3 = 8.96967f,
        )
        // L 11.8232 14.7626
        lineTo(x = 11.8232f, y = 14.7626f)
        // C 11.9209 14.8602 12.0791 14.8602 12.1768 14.7626
        curveTo(
          x1 = 11.9209f,
          y1 = 14.8602f,
          x2 = 12.0791f,
          y2 = 14.8602f,
          x3 = 12.1768f,
          y3 = 14.7626f,
        )
        // L 17.9697 8.96967
        lineTo(x = 17.9697f, y = 8.96967f)
        // C 18.2626 8.67678 18.7374 8.67678 19.0303 8.96967
        curveTo(
          x1 = 18.2626f,
          y1 = 8.67678f,
          x2 = 18.7374f,
          y2 = 8.67678f,
          x3 = 19.0303f,
          y3 = 8.96967f,
        )
        // C 19.3232 9.26256 19.3232 9.73744 19.0303 10.0303
        curveTo(
          x1 = 19.3232f,
          y1 = 9.26256f,
          x2 = 19.3232f,
          y2 = 9.73744f,
          x3 = 19.0303f,
          y3 = 10.0303f,
        )
        // L 13.2374 15.8232
        lineTo(x = 13.2374f, y = 15.8232f)
        // C 12.554 16.5066 11.446 16.5066 10.7626 15.8232
        curveTo(
          x1 = 12.554f,
          y1 = 16.5066f,
          x2 = 11.446f,
          y2 = 16.5066f,
          x3 = 10.7626f,
          y3 = 15.8232f,
        )
        // L 4.96967 10.0303
        lineTo(x = 4.96967f, y = 10.0303f)
        // C 4.67678 9.73744 4.67678 9.26256 4.96967 8.96967z
        curveTo(
          x1 = 4.67678f,
          y1 = 9.73744f,
          x2 = 4.67678f,
          y2 = 9.26256f,
          x3 = 4.96967f,
          y3 = 8.96967f,
        )
        close()
      }
    }.build().also { _chevronDown = it }
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
        imageVector = HedvigIcons.ChevronDown,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronDown: ImageVector? = null
