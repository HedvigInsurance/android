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
val HedvigIcons.Swap: ImageVector
  get() {
    val current = _swap
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Swap",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M11.9697 7.53018 L9.75 5.31051 V12.9999 C9.75 13.4141 9.41421 13.7499 9 13.7499 C8.58579 13.7499 8.25 13.4141 8.25 12.9999 V5.31051 L6.03033 7.53018 C5.73744 7.82307 5.26256 7.82307 4.96967 7.53018 C4.67678 7.23729 4.67678 6.76241 4.96967 6.46952 L7.76256 3.67663 C8.44598 2.99321 9.55402 2.99321 10.2374 3.67663 L13.0303 6.46952 C13.3232 6.76241 13.3232 7.23729 13.0303 7.53018 C12.7374 7.82307 12.2626 7.82307 11.9697 7.53018Z M15.75 18.6892 L17.9697 16.4696 C18.2626 16.1767 18.7374 16.1767 19.0303 16.4696 C19.3232 16.7625 19.3232 17.2373 19.0303 17.5302 L16.2374 20.3231 C15.554 21.0065 14.446 21.0065 13.7626 20.3231 L10.9697 17.5302 C10.6768 17.2373 10.6768 16.7625 10.9697 16.4696 C11.2626 16.1767 11.7374 16.1767 12.0303 16.4696 L14.25 18.6892 L14.25 10.9999 C14.25 10.5857 14.5858 10.2499 15 10.2499 C15.4142 10.2499 15.75 10.5857 15.75 10.9999 L15.75 18.6892Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 11.9697 7.53018
        moveTo(x = 11.9697f, y = 7.53018f)
        // L 9.75 5.31051
        lineTo(x = 9.75f, y = 5.31051f)
        // V 12.9999
        verticalLineTo(y = 12.9999f)
        // C 9.75 13.4141 9.41421 13.7499 9 13.7499
        curveTo(
          x1 = 9.75f,
          y1 = 13.4141f,
          x2 = 9.41421f,
          y2 = 13.7499f,
          x3 = 9.0f,
          y3 = 13.7499f,
        )
        // C 8.58579 13.7499 8.25 13.4141 8.25 12.9999
        curveTo(
          x1 = 8.58579f,
          y1 = 13.7499f,
          x2 = 8.25f,
          y2 = 13.4141f,
          x3 = 8.25f,
          y3 = 12.9999f,
        )
        // V 5.31051
        verticalLineTo(y = 5.31051f)
        // L 6.03033 7.53018
        lineTo(x = 6.03033f, y = 7.53018f)
        // C 5.73744 7.82307 5.26256 7.82307 4.96967 7.53018
        curveTo(
          x1 = 5.73744f,
          y1 = 7.82307f,
          x2 = 5.26256f,
          y2 = 7.82307f,
          x3 = 4.96967f,
          y3 = 7.53018f,
        )
        // C 4.67678 7.23729 4.67678 6.76241 4.96967 6.46952
        curveTo(
          x1 = 4.67678f,
          y1 = 7.23729f,
          x2 = 4.67678f,
          y2 = 6.76241f,
          x3 = 4.96967f,
          y3 = 6.46952f,
        )
        // L 7.76256 3.67663
        lineTo(x = 7.76256f, y = 3.67663f)
        // C 8.44598 2.99321 9.55402 2.99321 10.2374 3.67663
        curveTo(
          x1 = 8.44598f,
          y1 = 2.99321f,
          x2 = 9.55402f,
          y2 = 2.99321f,
          x3 = 10.2374f,
          y3 = 3.67663f,
        )
        // L 13.0303 6.46952
        lineTo(x = 13.0303f, y = 6.46952f)
        // C 13.3232 6.76241 13.3232 7.23729 13.0303 7.53018
        curveTo(
          x1 = 13.3232f,
          y1 = 6.76241f,
          x2 = 13.3232f,
          y2 = 7.23729f,
          x3 = 13.0303f,
          y3 = 7.53018f,
        )
        // C 12.7374 7.82307 12.2626 7.82307 11.9697 7.53018z
        curveTo(
          x1 = 12.7374f,
          y1 = 7.82307f,
          x2 = 12.2626f,
          y2 = 7.82307f,
          x3 = 11.9697f,
          y3 = 7.53018f,
        )
        close()
        // M 15.75 18.6892
        moveTo(x = 15.75f, y = 18.6892f)
        // L 17.9697 16.4696
        lineTo(x = 17.9697f, y = 16.4696f)
        // C 18.2626 16.1767 18.7374 16.1767 19.0303 16.4696
        curveTo(
          x1 = 18.2626f,
          y1 = 16.1767f,
          x2 = 18.7374f,
          y2 = 16.1767f,
          x3 = 19.0303f,
          y3 = 16.4696f,
        )
        // C 19.3232 16.7625 19.3232 17.2373 19.0303 17.5302
        curveTo(
          x1 = 19.3232f,
          y1 = 16.7625f,
          x2 = 19.3232f,
          y2 = 17.2373f,
          x3 = 19.0303f,
          y3 = 17.5302f,
        )
        // L 16.2374 20.3231
        lineTo(x = 16.2374f, y = 20.3231f)
        // C 15.554 21.0065 14.446 21.0065 13.7626 20.3231
        curveTo(
          x1 = 15.554f,
          y1 = 21.0065f,
          x2 = 14.446f,
          y2 = 21.0065f,
          x3 = 13.7626f,
          y3 = 20.3231f,
        )
        // L 10.9697 17.5302
        lineTo(x = 10.9697f, y = 17.5302f)
        // C 10.6768 17.2373 10.6768 16.7625 10.9697 16.4696
        curveTo(
          x1 = 10.6768f,
          y1 = 17.2373f,
          x2 = 10.6768f,
          y2 = 16.7625f,
          x3 = 10.9697f,
          y3 = 16.4696f,
        )
        // C 11.2626 16.1767 11.7374 16.1767 12.0303 16.4696
        curveTo(
          x1 = 11.2626f,
          y1 = 16.1767f,
          x2 = 11.7374f,
          y2 = 16.1767f,
          x3 = 12.0303f,
          y3 = 16.4696f,
        )
        // L 14.25 18.6892
        lineTo(x = 14.25f, y = 18.6892f)
        // L 14.25 10.9999
        lineTo(x = 14.25f, y = 10.9999f)
        // C 14.25 10.5857 14.5858 10.2499 15 10.2499
        curveTo(
          x1 = 14.25f,
          y1 = 10.5857f,
          x2 = 14.5858f,
          y2 = 10.2499f,
          x3 = 15.0f,
          y3 = 10.2499f,
        )
        // C 15.4142 10.2499 15.75 10.5857 15.75 10.9999
        curveTo(
          x1 = 15.4142f,
          y1 = 10.2499f,
          x2 = 15.75f,
          y2 = 10.5857f,
          x3 = 15.75f,
          y3 = 10.9999f,
        )
        // L 15.75 18.6892z
        lineTo(x = 15.75f, y = 18.6892f)
        close()
      }
    }.build().also { _swap = it }
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
        imageVector = HedvigIcons.Swap,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _swap: ImageVector? = null
