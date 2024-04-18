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
val HedvigIcons.Refresh: ImageVector
  get() {
    val current = _refresh
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Refresh",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12.6894 5.25 H12 C11.7335 5.25 11.4699 5.26348 11.2099 5.28984 C7.30045 5.68616 4.25 8.98637 4.25 13 C4.25 17.2802 7.71979 20.75 12 20.75 C16.2802 20.75 19.75 17.2802 19.75 13 C19.75 11.5401 19.3456 10.1724 18.6424 9.00533 C18.4286 8.65055 17.9677 8.53624 17.6129 8.75002 C17.2581 8.9638 17.1438 9.42471 17.3576 9.77949 C17.924 10.7195 18.25 11.8205 18.25 13 C18.25 16.4518 15.4518 19.25 12 19.25 C8.54822 19.25 5.75 16.4518 5.75 13 C5.75 9.76404 8.20985 7.10166 11.3612 6.78219 C11.571 6.76092 11.7841 6.75 12 6.75 H12.6894 L11.4697 7.96967 C11.1768 8.26256 11.1768 8.73744 11.4697 9.03033 C11.7626 9.32322 12.2375 9.32322 12.5303 9.03033 L14.6768 6.88388 C15.1649 6.39573 15.1649 5.60427 14.6768 5.11612 L12.5303 2.96967 C12.2375 2.67678 11.7626 2.67678 11.4697 2.96967 C11.1768 3.26256 11.1768 3.73744 11.4697 4.03033 L12.6894 5.25Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12.6894 5.25
        moveTo(x = 12.6894f, y = 5.25f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // C 11.7335 5.25 11.4699 5.26348 11.2099 5.28984
        curveTo(
          x1 = 11.7335f,
          y1 = 5.25f,
          x2 = 11.4699f,
          y2 = 5.26348f,
          x3 = 11.2099f,
          y3 = 5.28984f,
        )
        // C 7.30045 5.68616 4.25 8.98637 4.25 13
        curveTo(
          x1 = 7.30045f,
          y1 = 5.68616f,
          x2 = 4.25f,
          y2 = 8.98637f,
          x3 = 4.25f,
          y3 = 13.0f,
        )
        // C 4.25 17.2802 7.71979 20.75 12 20.75
        curveTo(
          x1 = 4.25f,
          y1 = 17.2802f,
          x2 = 7.71979f,
          y2 = 20.75f,
          x3 = 12.0f,
          y3 = 20.75f,
        )
        // C 16.2802 20.75 19.75 17.2802 19.75 13
        curveTo(
          x1 = 16.2802f,
          y1 = 20.75f,
          x2 = 19.75f,
          y2 = 17.2802f,
          x3 = 19.75f,
          y3 = 13.0f,
        )
        // C 19.75 11.5401 19.3456 10.1724 18.6424 9.00533
        curveTo(
          x1 = 19.75f,
          y1 = 11.5401f,
          x2 = 19.3456f,
          y2 = 10.1724f,
          x3 = 18.6424f,
          y3 = 9.00533f,
        )
        // C 18.4286 8.65055 17.9677 8.53624 17.6129 8.75002
        curveTo(
          x1 = 18.4286f,
          y1 = 8.65055f,
          x2 = 17.9677f,
          y2 = 8.53624f,
          x3 = 17.6129f,
          y3 = 8.75002f,
        )
        // C 17.2581 8.9638 17.1438 9.42471 17.3576 9.77949
        curveTo(
          x1 = 17.2581f,
          y1 = 8.9638f,
          x2 = 17.1438f,
          y2 = 9.42471f,
          x3 = 17.3576f,
          y3 = 9.77949f,
        )
        // C 17.924 10.7195 18.25 11.8205 18.25 13
        curveTo(
          x1 = 17.924f,
          y1 = 10.7195f,
          x2 = 18.25f,
          y2 = 11.8205f,
          x3 = 18.25f,
          y3 = 13.0f,
        )
        // C 18.25 16.4518 15.4518 19.25 12 19.25
        curveTo(
          x1 = 18.25f,
          y1 = 16.4518f,
          x2 = 15.4518f,
          y2 = 19.25f,
          x3 = 12.0f,
          y3 = 19.25f,
        )
        // C 8.54822 19.25 5.75 16.4518 5.75 13
        curveTo(
          x1 = 8.54822f,
          y1 = 19.25f,
          x2 = 5.75f,
          y2 = 16.4518f,
          x3 = 5.75f,
          y3 = 13.0f,
        )
        // C 5.75 9.76404 8.20985 7.10166 11.3612 6.78219
        curveTo(
          x1 = 5.75f,
          y1 = 9.76404f,
          x2 = 8.20985f,
          y2 = 7.10166f,
          x3 = 11.3612f,
          y3 = 6.78219f,
        )
        // C 11.571 6.76092 11.7841 6.75 12 6.75
        curveTo(
          x1 = 11.571f,
          y1 = 6.76092f,
          x2 = 11.7841f,
          y2 = 6.75f,
          x3 = 12.0f,
          y3 = 6.75f,
        )
        // H 12.6894
        horizontalLineTo(x = 12.6894f)
        // L 11.4697 7.96967
        lineTo(x = 11.4697f, y = 7.96967f)
        // C 11.1768 8.26256 11.1768 8.73744 11.4697 9.03033
        curveTo(
          x1 = 11.1768f,
          y1 = 8.26256f,
          x2 = 11.1768f,
          y2 = 8.73744f,
          x3 = 11.4697f,
          y3 = 9.03033f,
        )
        // C 11.7626 9.32322 12.2375 9.32322 12.5303 9.03033
        curveTo(
          x1 = 11.7626f,
          y1 = 9.32322f,
          x2 = 12.2375f,
          y2 = 9.32322f,
          x3 = 12.5303f,
          y3 = 9.03033f,
        )
        // L 14.6768 6.88388
        lineTo(x = 14.6768f, y = 6.88388f)
        // C 15.1649 6.39573 15.1649 5.60427 14.6768 5.11612
        curveTo(
          x1 = 15.1649f,
          y1 = 6.39573f,
          x2 = 15.1649f,
          y2 = 5.60427f,
          x3 = 14.6768f,
          y3 = 5.11612f,
        )
        // L 12.5303 2.96967
        lineTo(x = 12.5303f, y = 2.96967f)
        // C 12.2375 2.67678 11.7626 2.67678 11.4697 2.96967
        curveTo(
          x1 = 12.2375f,
          y1 = 2.67678f,
          x2 = 11.7626f,
          y2 = 2.67678f,
          x3 = 11.4697f,
          y3 = 2.96967f,
        )
        // C 11.1768 3.26256 11.1768 3.73744 11.4697 4.03033
        curveTo(
          x1 = 11.1768f,
          y1 = 3.26256f,
          x2 = 11.1768f,
          y2 = 3.73744f,
          x3 = 11.4697f,
          y3 = 4.03033f,
        )
        // L 12.6894 5.25z
        lineTo(x = 12.6894f, y = 5.25f)
        close()
      }
    }.build().also { _refresh = it }
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
        imageVector = HedvigIcons.Refresh,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _refresh: ImageVector? = null
