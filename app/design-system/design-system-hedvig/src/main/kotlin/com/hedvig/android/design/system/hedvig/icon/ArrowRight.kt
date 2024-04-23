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
val HedvigIcons.ArrowRight: ImageVector
  get() {
    val current = _arrowRight
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ArrowRight",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M11.4697 17.9697 C11.1768 18.2626 11.1768 18.7374 11.4697 19.0303 C11.7626 19.3232 12.2374 19.3232 12.5303 19.0303 L18.3232 13.2374 C19.0066 12.554 19.0066 11.446 18.3232 10.7626 L12.5303 4.96967 C12.2374 4.67678 11.7626 4.67678 11.4697 4.96967 C11.1768 5.26256 11.1768 5.73744 11.4697 6.03033 L16.6893 11.25 L5 11.25 C4.58579 11.25 4.25 11.5858 4.25 12 C4.25 12.4142 4.58579 12.75 5 12.75 L16.6893 12.75 L11.4697 17.9697Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 11.4697 17.9697
        moveTo(x = 11.4697f, y = 17.9697f)
        // C 11.1768 18.2626 11.1768 18.7374 11.4697 19.0303
        curveTo(
          x1 = 11.1768f,
          y1 = 18.2626f,
          x2 = 11.1768f,
          y2 = 18.7374f,
          x3 = 11.4697f,
          y3 = 19.0303f,
        )
        // C 11.7626 19.3232 12.2374 19.3232 12.5303 19.0303
        curveTo(
          x1 = 11.7626f,
          y1 = 19.3232f,
          x2 = 12.2374f,
          y2 = 19.3232f,
          x3 = 12.5303f,
          y3 = 19.0303f,
        )
        // L 18.3232 13.2374
        lineTo(x = 18.3232f, y = 13.2374f)
        // C 19.0066 12.554 19.0066 11.446 18.3232 10.7626
        curveTo(
          x1 = 19.0066f,
          y1 = 12.554f,
          x2 = 19.0066f,
          y2 = 11.446f,
          x3 = 18.3232f,
          y3 = 10.7626f,
        )
        // L 12.5303 4.96967
        lineTo(x = 12.5303f, y = 4.96967f)
        // C 12.2374 4.67678 11.7626 4.67678 11.4697 4.96967
        curveTo(
          x1 = 12.2374f,
          y1 = 4.67678f,
          x2 = 11.7626f,
          y2 = 4.67678f,
          x3 = 11.4697f,
          y3 = 4.96967f,
        )
        // C 11.1768 5.26256 11.1768 5.73744 11.4697 6.03033
        curveTo(
          x1 = 11.1768f,
          y1 = 5.26256f,
          x2 = 11.1768f,
          y2 = 5.73744f,
          x3 = 11.4697f,
          y3 = 6.03033f,
        )
        // L 16.6893 11.25
        lineTo(x = 16.6893f, y = 11.25f)
        // L 5 11.25
        lineTo(x = 5.0f, y = 11.25f)
        // C 4.58579 11.25 4.25 11.5858 4.25 12
        curveTo(
          x1 = 4.58579f,
          y1 = 11.25f,
          x2 = 4.25f,
          y2 = 11.5858f,
          x3 = 4.25f,
          y3 = 12.0f,
        )
        // C 4.25 12.4142 4.58579 12.75 5 12.75
        curveTo(
          x1 = 4.25f,
          y1 = 12.4142f,
          x2 = 4.58579f,
          y2 = 12.75f,
          x3 = 5.0f,
          y3 = 12.75f,
        )
        // L 16.6893 12.75
        lineTo(x = 16.6893f, y = 12.75f)
        // L 11.4697 17.9697z
        lineTo(x = 11.4697f, y = 17.9697f)
        close()
      }
    }.build().also { _arrowRight = it }
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
        imageVector = HedvigIcons.ArrowRight,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowRight: ImageVector? = null
