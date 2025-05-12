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
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ArrowDown: ImageVector
  get() {
    val current = _arrowDown
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ArrowDown",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M6.03033 11.4698 C5.73744 11.1769 5.26256 11.1769 4.96967 11.4698 C4.67678 11.7627 4.67678 12.2376 4.96967 12.5305 L10.7626 18.3234 C11.446 19.0068 12.554 19.0068 13.2374 18.3234 L19.0303 12.5305 C19.3232 12.2376 19.3232 11.7627 19.0303 11.4698 C18.7374 11.1769 18.2626 11.1769 17.9697 11.4698 L12.75 16.6895 L12.75 5.00012 C12.75 4.58591 12.4142 4.25012 12 4.25012 C11.5858 4.25012 11.25 4.58591 11.25 5.00012 L11.25 16.6895 L6.03033 11.4698Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 6.03033 11.4698
        moveTo(x = 6.03033f, y = 11.4698f)
        // C 5.73744 11.1769 5.26256 11.1769 4.96967 11.4698
        curveTo(
          x1 = 5.73744f,
          y1 = 11.1769f,
          x2 = 5.26256f,
          y2 = 11.1769f,
          x3 = 4.96967f,
          y3 = 11.4698f,
        )
        // C 4.67678 11.7627 4.67678 12.2376 4.96967 12.5305
        curveTo(
          x1 = 4.67678f,
          y1 = 11.7627f,
          x2 = 4.67678f,
          y2 = 12.2376f,
          x3 = 4.96967f,
          y3 = 12.5305f,
        )
        // L 10.7626 18.3234
        lineTo(x = 10.7626f, y = 18.3234f)
        // C 11.446 19.0068 12.554 19.0068 13.2374 18.3234
        curveTo(
          x1 = 11.446f,
          y1 = 19.0068f,
          x2 = 12.554f,
          y2 = 19.0068f,
          x3 = 13.2374f,
          y3 = 18.3234f,
        )
        // L 19.0303 12.5305
        lineTo(x = 19.0303f, y = 12.5305f)
        // C 19.3232 12.2376 19.3232 11.7627 19.0303 11.4698
        curveTo(
          x1 = 19.3232f,
          y1 = 12.2376f,
          x2 = 19.3232f,
          y2 = 11.7627f,
          x3 = 19.0303f,
          y3 = 11.4698f,
        )
        // C 18.7374 11.1769 18.2626 11.1769 17.9697 11.4698
        curveTo(
          x1 = 18.7374f,
          y1 = 11.1769f,
          x2 = 18.2626f,
          y2 = 11.1769f,
          x3 = 17.9697f,
          y3 = 11.4698f,
        )
        // L 12.75 16.6895
        lineTo(x = 12.75f, y = 16.6895f)
        // L 12.75 5.00012
        lineTo(x = 12.75f, y = 5.00012f)
        // C 12.75 4.58591 12.4142 4.25012 12 4.25012
        curveTo(
          x1 = 12.75f,
          y1 = 4.58591f,
          x2 = 12.4142f,
          y2 = 4.25012f,
          x3 = 12.0f,
          y3 = 4.25012f,
        )
        // C 11.5858 4.25012 11.25 4.58591 11.25 5.00012
        curveTo(
          x1 = 11.5858f,
          y1 = 4.25012f,
          x2 = 11.25f,
          y2 = 4.58591f,
          x3 = 11.25f,
          y3 = 5.00012f,
        )
        // L 11.25 16.6895
        lineTo(x = 11.25f, y = 16.6895f)
        // L 6.03033 11.4698z
        lineTo(x = 6.03033f, y = 11.4698f)
        close()
      }
    }.build().also { _arrowDown = it }
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
        imageVector = HedvigIcons.ArrowDown,
        contentDescription = EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _arrowDown: ImageVector? = null
