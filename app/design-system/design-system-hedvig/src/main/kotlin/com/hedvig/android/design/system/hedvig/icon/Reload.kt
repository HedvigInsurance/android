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
val HedvigIcons.Reload: ImageVector
  get() {
    val current = _reload
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Reload",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 4.25 H12.6894 L11.4697 3.03033 C11.1768 2.73744 11.1768 2.26256 11.4697 1.96967 C11.7626 1.67678 12.2375 1.67678 12.5303 1.96967 L14.6768 4.11612 C15.1649 4.60427 15.1649 5.39573 14.6768 5.88388 L12.5303 8.03033 C12.2375 8.32322 11.7626 8.32322 11.4697 8.03033 C11.1768 7.73744 11.1768 7.26256 11.4697 6.96967 L12.6894 5.75 H12 C8.54822 5.75 5.75 8.54822 5.75 12 C5.75 13.1396 6.05434 14.2059 6.58568 15.1245 C6.79309 15.483 6.67057 15.9418 6.31202 16.1492 C5.95347 16.3566 5.49468 16.2341 5.28727 15.8755 C4.62742 14.7349 4.25 13.4104 4.25 12 C4.25 7.71979 7.71979 4.25 12 4.25Z M18.25 12 C18.25 15.4518 15.4518 18.25 12 18.25 H11.3106 L12.5303 17.0303 C12.8232 16.7374 12.8232 16.2626 12.5303 15.9697 C12.2374 15.6768 11.7625 15.6768 11.4697 15.9697 L9.32321 18.1161 C8.83505 18.6043 8.83505 19.3957 9.32321 19.8839 L11.4697 22.0303 C11.7625 22.3232 12.2374 22.3232 12.5303 22.0303 C12.8232 21.7374 12.8232 21.2626 12.5303 20.9697 L11.3106 19.75 H12 C16.2802 19.75 19.75 16.2802 19.75 12 C19.75 10.5896 19.3726 9.26514 18.7127 8.12446 C18.5053 7.76591 18.0465 7.64339 17.688 7.8508 C17.3294 8.0582 17.2069 8.517 17.4143 8.87554 C17.9457 9.79407 18.25 10.8604 18.25 12Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12 4.25
        moveTo(x = 12.0f, y = 4.25f)
        // H 12.6894
        horizontalLineTo(x = 12.6894f)
        // L 11.4697 3.03033
        lineTo(x = 11.4697f, y = 3.03033f)
        // C 11.1768 2.73744 11.1768 2.26256 11.4697 1.96967
        curveTo(
          x1 = 11.1768f,
          y1 = 2.73744f,
          x2 = 11.1768f,
          y2 = 2.26256f,
          x3 = 11.4697f,
          y3 = 1.96967f,
        )
        // C 11.7626 1.67678 12.2375 1.67678 12.5303 1.96967
        curveTo(
          x1 = 11.7626f,
          y1 = 1.67678f,
          x2 = 12.2375f,
          y2 = 1.67678f,
          x3 = 12.5303f,
          y3 = 1.96967f,
        )
        // L 14.6768 4.11612
        lineTo(x = 14.6768f, y = 4.11612f)
        // C 15.1649 4.60427 15.1649 5.39573 14.6768 5.88388
        curveTo(
          x1 = 15.1649f,
          y1 = 4.60427f,
          x2 = 15.1649f,
          y2 = 5.39573f,
          x3 = 14.6768f,
          y3 = 5.88388f,
        )
        // L 12.5303 8.03033
        lineTo(x = 12.5303f, y = 8.03033f)
        // C 12.2375 8.32322 11.7626 8.32322 11.4697 8.03033
        curveTo(
          x1 = 12.2375f,
          y1 = 8.32322f,
          x2 = 11.7626f,
          y2 = 8.32322f,
          x3 = 11.4697f,
          y3 = 8.03033f,
        )
        // C 11.1768 7.73744 11.1768 7.26256 11.4697 6.96967
        curveTo(
          x1 = 11.1768f,
          y1 = 7.73744f,
          x2 = 11.1768f,
          y2 = 7.26256f,
          x3 = 11.4697f,
          y3 = 6.96967f,
        )
        // L 12.6894 5.75
        lineTo(x = 12.6894f, y = 5.75f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // C 8.54822 5.75 5.75 8.54822 5.75 12
        curveTo(
          x1 = 8.54822f,
          y1 = 5.75f,
          x2 = 5.75f,
          y2 = 8.54822f,
          x3 = 5.75f,
          y3 = 12.0f,
        )
        // C 5.75 13.1396 6.05434 14.2059 6.58568 15.1245
        curveTo(
          x1 = 5.75f,
          y1 = 13.1396f,
          x2 = 6.05434f,
          y2 = 14.2059f,
          x3 = 6.58568f,
          y3 = 15.1245f,
        )
        // C 6.79309 15.483 6.67057 15.9418 6.31202 16.1492
        curveTo(
          x1 = 6.79309f,
          y1 = 15.483f,
          x2 = 6.67057f,
          y2 = 15.9418f,
          x3 = 6.31202f,
          y3 = 16.1492f,
        )
        // C 5.95347 16.3566 5.49468 16.2341 5.28727 15.8755
        curveTo(
          x1 = 5.95347f,
          y1 = 16.3566f,
          x2 = 5.49468f,
          y2 = 16.2341f,
          x3 = 5.28727f,
          y3 = 15.8755f,
        )
        // C 4.62742 14.7349 4.25 13.4104 4.25 12
        curveTo(
          x1 = 4.62742f,
          y1 = 14.7349f,
          x2 = 4.25f,
          y2 = 13.4104f,
          x3 = 4.25f,
          y3 = 12.0f,
        )
        // C 4.25 7.71979 7.71979 4.25 12 4.25z
        curveTo(
          x1 = 4.25f,
          y1 = 7.71979f,
          x2 = 7.71979f,
          y2 = 4.25f,
          x3 = 12.0f,
          y3 = 4.25f,
        )
        close()
        // M 18.25 12
        moveTo(x = 18.25f, y = 12.0f)
        // C 18.25 15.4518 15.4518 18.25 12 18.25
        curveTo(
          x1 = 18.25f,
          y1 = 15.4518f,
          x2 = 15.4518f,
          y2 = 18.25f,
          x3 = 12.0f,
          y3 = 18.25f,
        )
        // H 11.3106
        horizontalLineTo(x = 11.3106f)
        // L 12.5303 17.0303
        lineTo(x = 12.5303f, y = 17.0303f)
        // C 12.8232 16.7374 12.8232 16.2626 12.5303 15.9697
        curveTo(
          x1 = 12.8232f,
          y1 = 16.7374f,
          x2 = 12.8232f,
          y2 = 16.2626f,
          x3 = 12.5303f,
          y3 = 15.9697f,
        )
        // C 12.2374 15.6768 11.7625 15.6768 11.4697 15.9697
        curveTo(
          x1 = 12.2374f,
          y1 = 15.6768f,
          x2 = 11.7625f,
          y2 = 15.6768f,
          x3 = 11.4697f,
          y3 = 15.9697f,
        )
        // L 9.32321 18.1161
        lineTo(x = 9.32321f, y = 18.1161f)
        // C 8.83505 18.6043 8.83505 19.3957 9.32321 19.8839
        curveTo(
          x1 = 8.83505f,
          y1 = 18.6043f,
          x2 = 8.83505f,
          y2 = 19.3957f,
          x3 = 9.32321f,
          y3 = 19.8839f,
        )
        // L 11.4697 22.0303
        lineTo(x = 11.4697f, y = 22.0303f)
        // C 11.7625 22.3232 12.2374 22.3232 12.5303 22.0303
        curveTo(
          x1 = 11.7625f,
          y1 = 22.3232f,
          x2 = 12.2374f,
          y2 = 22.3232f,
          x3 = 12.5303f,
          y3 = 22.0303f,
        )
        // C 12.8232 21.7374 12.8232 21.2626 12.5303 20.9697
        curveTo(
          x1 = 12.8232f,
          y1 = 21.7374f,
          x2 = 12.8232f,
          y2 = 21.2626f,
          x3 = 12.5303f,
          y3 = 20.9697f,
        )
        // L 11.3106 19.75
        lineTo(x = 11.3106f, y = 19.75f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // C 16.2802 19.75 19.75 16.2802 19.75 12
        curveTo(
          x1 = 16.2802f,
          y1 = 19.75f,
          x2 = 19.75f,
          y2 = 16.2802f,
          x3 = 19.75f,
          y3 = 12.0f,
        )
        // C 19.75 10.5896 19.3726 9.26514 18.7127 8.12446
        curveTo(
          x1 = 19.75f,
          y1 = 10.5896f,
          x2 = 19.3726f,
          y2 = 9.26514f,
          x3 = 18.7127f,
          y3 = 8.12446f,
        )
        // C 18.5053 7.76591 18.0465 7.64339 17.688 7.8508
        curveTo(
          x1 = 18.5053f,
          y1 = 7.76591f,
          x2 = 18.0465f,
          y2 = 7.64339f,
          x3 = 17.688f,
          y3 = 7.8508f,
        )
        // C 17.3294 8.0582 17.2069 8.517 17.4143 8.87554
        curveTo(
          x1 = 17.3294f,
          y1 = 8.0582f,
          x2 = 17.2069f,
          y2 = 8.517f,
          x3 = 17.4143f,
          y3 = 8.87554f,
        )
        // C 17.9457 9.79407 18.25 10.8604 18.25 12z
        curveTo(
          x1 = 17.9457f,
          y1 = 9.79407f,
          x2 = 18.25f,
          y2 = 10.8604f,
          x3 = 18.25f,
          y3 = 12.0f,
        )
        close()
      }
    }.build().also { _reload = it }
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
        imageVector = HedvigIcons.Reload,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _reload: ImageVector? = null
