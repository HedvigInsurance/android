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
val HedvigIcons.ArrowLeft: ImageVector
  get() {
    val current = _arrowLeft
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ArrowLeft",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12.5303 6.03033 C12.8232 5.73744 12.8232 5.26256 12.5303 4.96967 C12.2374 4.67678 11.7626 4.67678 11.4697 4.96967 L5.67678 10.7626 C4.99336 11.446 4.99336 12.554 5.67678 13.2374 L11.4697 19.0303 C11.7626 19.3232 12.2374 19.3232 12.5303 19.0303 C12.8232 18.7374 12.8232 18.2626 12.5303 17.9697 L7.31066 12.75 H19 C19.4142 12.75 19.75 12.4142 19.75 12 C19.75 11.5858 19.4142 11.25 19 11.25 H7.31066 L12.5303 6.03033Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12.5303 6.03033
        moveTo(x = 12.5303f, y = 6.03033f)
        // C 12.8232 5.73744 12.8232 5.26256 12.5303 4.96967
        curveTo(
          x1 = 12.8232f,
          y1 = 5.73744f,
          x2 = 12.8232f,
          y2 = 5.26256f,
          x3 = 12.5303f,
          y3 = 4.96967f,
        )
        // C 12.2374 4.67678 11.7626 4.67678 11.4697 4.96967
        curveTo(
          x1 = 12.2374f,
          y1 = 4.67678f,
          x2 = 11.7626f,
          y2 = 4.67678f,
          x3 = 11.4697f,
          y3 = 4.96967f,
        )
        // L 5.67678 10.7626
        lineTo(x = 5.67678f, y = 10.7626f)
        // C 4.99336 11.446 4.99336 12.554 5.67678 13.2374
        curveTo(
          x1 = 4.99336f,
          y1 = 11.446f,
          x2 = 4.99336f,
          y2 = 12.554f,
          x3 = 5.67678f,
          y3 = 13.2374f,
        )
        // L 11.4697 19.0303
        lineTo(x = 11.4697f, y = 19.0303f)
        // C 11.7626 19.3232 12.2374 19.3232 12.5303 19.0303
        curveTo(
          x1 = 11.7626f,
          y1 = 19.3232f,
          x2 = 12.2374f,
          y2 = 19.3232f,
          x3 = 12.5303f,
          y3 = 19.0303f,
        )
        // C 12.8232 18.7374 12.8232 18.2626 12.5303 17.9697
        curveTo(
          x1 = 12.8232f,
          y1 = 18.7374f,
          x2 = 12.8232f,
          y2 = 18.2626f,
          x3 = 12.5303f,
          y3 = 17.9697f,
        )
        // L 7.31066 12.75
        lineTo(x = 7.31066f, y = 12.75f)
        // H 19
        horizontalLineTo(x = 19.0f)
        // C 19.4142 12.75 19.75 12.4142 19.75 12
        curveTo(
          x1 = 19.4142f,
          y1 = 12.75f,
          x2 = 19.75f,
          y2 = 12.4142f,
          x3 = 19.75f,
          y3 = 12.0f,
        )
        // C 19.75 11.5858 19.4142 11.25 19 11.25
        curveTo(
          x1 = 19.75f,
          y1 = 11.5858f,
          x2 = 19.4142f,
          y2 = 11.25f,
          x3 = 19.0f,
          y3 = 11.25f,
        )
        // H 7.31066
        horizontalLineTo(x = 7.31066f)
        // L 12.5303 6.03033z
        lineTo(x = 12.5303f, y = 6.03033f)
        close()
      }
    }.build().also { _arrowLeft = it }
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
        imageVector = HedvigIcons.ArrowLeft,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowLeft: ImageVector? = null
