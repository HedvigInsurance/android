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
val HedvigIcons.Download: ImageVector
  get() {
    val current = _download
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Download",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12.75 13.1892 L14.9697 10.9696 C15.2626 10.6767 15.7374 10.6767 16.0303 10.9696 C16.3232 11.2625 16.3232 11.7373 16.0303 12.0302 L13.2374 14.8231 C12.554 15.5065 11.446 15.5065 10.7626 14.8231 L7.96967 12.0302 C7.67678 11.7373 7.67678 11.2625 7.96967 10.9696 C8.26256 10.6767 8.73744 10.6767 9.03033 10.9696 L11.25 13.1892 L11.25 5.5 C11.25 5.08579 11.5858 4.75 12 4.75 C12.4142 4.75 12.75 5.08579 12.75 5.5 L12.75 13.1892Z M5 14.75 C5.41421 14.75 5.75 15.0858 5.75 15.5 V17.5 C5.75 17.6381 5.86193 17.75 6 17.75 H18 C18.1381 17.75 18.25 17.6381 18.25 17.5 V15.5 C18.25 15.0858 18.5858 14.75 19 14.75 C19.4142 14.75 19.75 15.0858 19.75 15.5 V17.5 C19.75 18.4665 18.9665 19.25 18 19.25 H6 C5.0335 19.25 4.25 18.4665 4.25 17.5 V15.5 C4.25 15.0858 4.58579 14.75 5 14.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 12.75 13.1892
        moveTo(x = 12.75f, y = 13.1892f)
        // L 14.9697 10.9696
        lineTo(x = 14.9697f, y = 10.9696f)
        // C 15.2626 10.6767 15.7374 10.6767 16.0303 10.9696
        curveTo(
          x1 = 15.2626f,
          y1 = 10.6767f,
          x2 = 15.7374f,
          y2 = 10.6767f,
          x3 = 16.0303f,
          y3 = 10.9696f,
        )
        // C 16.3232 11.2625 16.3232 11.7373 16.0303 12.0302
        curveTo(
          x1 = 16.3232f,
          y1 = 11.2625f,
          x2 = 16.3232f,
          y2 = 11.7373f,
          x3 = 16.0303f,
          y3 = 12.0302f,
        )
        // L 13.2374 14.8231
        lineTo(x = 13.2374f, y = 14.8231f)
        // C 12.554 15.5065 11.446 15.5065 10.7626 14.8231
        curveTo(
          x1 = 12.554f,
          y1 = 15.5065f,
          x2 = 11.446f,
          y2 = 15.5065f,
          x3 = 10.7626f,
          y3 = 14.8231f,
        )
        // L 7.96967 12.0302
        lineTo(x = 7.96967f, y = 12.0302f)
        // C 7.67678 11.7373 7.67678 11.2625 7.96967 10.9696
        curveTo(
          x1 = 7.67678f,
          y1 = 11.7373f,
          x2 = 7.67678f,
          y2 = 11.2625f,
          x3 = 7.96967f,
          y3 = 10.9696f,
        )
        // C 8.26256 10.6767 8.73744 10.6767 9.03033 10.9696
        curveTo(
          x1 = 8.26256f,
          y1 = 10.6767f,
          x2 = 8.73744f,
          y2 = 10.6767f,
          x3 = 9.03033f,
          y3 = 10.9696f,
        )
        // L 11.25 13.1892
        lineTo(x = 11.25f, y = 13.1892f)
        // L 11.25 5.5
        lineTo(x = 11.25f, y = 5.5f)
        // C 11.25 5.08579 11.5858 4.75 12 4.75
        curveTo(
          x1 = 11.25f,
          y1 = 5.08579f,
          x2 = 11.5858f,
          y2 = 4.75f,
          x3 = 12.0f,
          y3 = 4.75f,
        )
        // C 12.4142 4.75 12.75 5.08579 12.75 5.5
        curveTo(
          x1 = 12.4142f,
          y1 = 4.75f,
          x2 = 12.75f,
          y2 = 5.08579f,
          x3 = 12.75f,
          y3 = 5.5f,
        )
        // L 12.75 13.1892z
        lineTo(x = 12.75f, y = 13.1892f)
        close()
        // M 5 14.75
        moveTo(x = 5.0f, y = 14.75f)
        // C 5.41421 14.75 5.75 15.0858 5.75 15.5
        curveTo(
          x1 = 5.41421f,
          y1 = 14.75f,
          x2 = 5.75f,
          y2 = 15.0858f,
          x3 = 5.75f,
          y3 = 15.5f,
        )
        // V 17.5
        verticalLineTo(y = 17.5f)
        // C 5.75 17.6381 5.86193 17.75 6 17.75
        curveTo(
          x1 = 5.75f,
          y1 = 17.6381f,
          x2 = 5.86193f,
          y2 = 17.75f,
          x3 = 6.0f,
          y3 = 17.75f,
        )
        // H 18
        horizontalLineTo(x = 18.0f)
        // C 18.1381 17.75 18.25 17.6381 18.25 17.5
        curveTo(
          x1 = 18.1381f,
          y1 = 17.75f,
          x2 = 18.25f,
          y2 = 17.6381f,
          x3 = 18.25f,
          y3 = 17.5f,
        )
        // V 15.5
        verticalLineTo(y = 15.5f)
        // C 18.25 15.0858 18.5858 14.75 19 14.75
        curveTo(
          x1 = 18.25f,
          y1 = 15.0858f,
          x2 = 18.5858f,
          y2 = 14.75f,
          x3 = 19.0f,
          y3 = 14.75f,
        )
        // C 19.4142 14.75 19.75 15.0858 19.75 15.5
        curveTo(
          x1 = 19.4142f,
          y1 = 14.75f,
          x2 = 19.75f,
          y2 = 15.0858f,
          x3 = 19.75f,
          y3 = 15.5f,
        )
        // V 17.5
        verticalLineTo(y = 17.5f)
        // C 19.75 18.4665 18.9665 19.25 18 19.25
        curveTo(
          x1 = 19.75f,
          y1 = 18.4665f,
          x2 = 18.9665f,
          y2 = 19.25f,
          x3 = 18.0f,
          y3 = 19.25f,
        )
        // H 6
        horizontalLineTo(x = 6.0f)
        // C 5.0335 19.25 4.25 18.4665 4.25 17.5
        curveTo(
          x1 = 5.0335f,
          y1 = 19.25f,
          x2 = 4.25f,
          y2 = 18.4665f,
          x3 = 4.25f,
          y3 = 17.5f,
        )
        // V 15.5
        verticalLineTo(y = 15.5f)
        // C 4.25 15.0858 4.58579 14.75 5 14.75z
        curveTo(
          x1 = 4.25f,
          y1 = 15.0858f,
          x2 = 4.58579f,
          y2 = 14.75f,
          x3 = 5.0f,
          y3 = 14.75f,
        )
        close()
      }
    }.build().also { _download = it }
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
        imageVector = HedvigIcons.Download,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _download: ImageVector? = null
