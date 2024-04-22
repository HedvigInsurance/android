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
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ChevronRight: ImageVector
  get() {
    val current = _chevronRight
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ChevronRight",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.46967 19.0303 C9.17678 18.7374 9.17678 18.2626 9.46967 17.9697 L15.2626 12.1768 C15.3602 12.0791 15.3602 11.9209 15.2626 11.8232 L9.46967 6.03033 C9.17678 5.73744 9.17678 5.26256 9.46967 4.96967 C9.76256 4.67678 10.2374 4.67678 10.5303 4.96967 L16.3232 10.7626 C17.0066 11.446 17.0066 12.554 16.3232 13.2374 L10.5303 19.0303 C10.2374 19.3232 9.76256 19.3232 9.46967 19.0303Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 9.46967 19.0303
        moveTo(x = 9.46967f, y = 19.0303f)
        // C 9.17678 18.7374 9.17678 18.2626 9.46967 17.9697
        curveTo(
          x1 = 9.17678f,
          y1 = 18.7374f,
          x2 = 9.17678f,
          y2 = 18.2626f,
          x3 = 9.46967f,
          y3 = 17.9697f,
        )
        // L 15.2626 12.1768
        lineTo(x = 15.2626f, y = 12.1768f)
        // C 15.3602 12.0791 15.3602 11.9209 15.2626 11.8232
        curveTo(
          x1 = 15.3602f,
          y1 = 12.0791f,
          x2 = 15.3602f,
          y2 = 11.9209f,
          x3 = 15.2626f,
          y3 = 11.8232f,
        )
        // L 9.46967 6.03033
        lineTo(x = 9.46967f, y = 6.03033f)
        // C 9.17678 5.73744 9.17678 5.26256 9.46967 4.96967
        curveTo(
          x1 = 9.17678f,
          y1 = 5.73744f,
          x2 = 9.17678f,
          y2 = 5.26256f,
          x3 = 9.46967f,
          y3 = 4.96967f,
        )
        // C 9.76256 4.67678 10.2374 4.67678 10.5303 4.96967
        curveTo(
          x1 = 9.76256f,
          y1 = 4.67678f,
          x2 = 10.2374f,
          y2 = 4.67678f,
          x3 = 10.5303f,
          y3 = 4.96967f,
        )
        // L 16.3232 10.7626
        lineTo(x = 16.3232f, y = 10.7626f)
        // C 17.0066 11.446 17.0066 12.554 16.3232 13.2374
        curveTo(
          x1 = 17.0066f,
          y1 = 11.446f,
          x2 = 17.0066f,
          y2 = 12.554f,
          x3 = 16.3232f,
          y3 = 13.2374f,
        )
        // L 10.5303 19.0303
        lineTo(x = 10.5303f, y = 19.0303f)
        // C 10.2374 19.3232 9.76256 19.3232 9.46967 19.0303z
        curveTo(
          x1 = 10.2374f,
          y1 = 19.3232f,
          x2 = 9.76256f,
          y2 = 19.3232f,
          x3 = 9.46967f,
          y3 = 19.0303f,
        )
        close()
      }
    }.build().also { _chevronRight = it }
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
        imageVector = HedvigIcons.ChevronRight,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronRight: ImageVector? = null
