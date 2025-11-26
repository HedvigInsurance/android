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
val HedvigIcons.ChevronUp: ImageVector
  get() {
    val current = _chevronUp
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ChevronUp",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19.0303 14.5304 C18.7374 14.8233 18.2626 14.8233 17.9697 14.5304 L12.1768 8.73753 C12.0791 8.6399 11.9209 8.6399 11.8232 8.73753 L6.03033 14.5304 C5.73744 14.8233 5.26256 14.8233 4.96967 14.5304 C4.67678 14.2375 4.67678 13.7627 4.96967 13.4698 L10.7626 7.67687 C11.446 6.99345 12.554 6.99345 13.2374 7.67687 L19.0303 13.4698 C19.3232 13.7627 19.3232 14.2375 19.0303 14.5304Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 19.0303 14.5304
        moveTo(x = 19.0303f, y = 14.5304f)
        // C 18.7374 14.8233 18.2626 14.8233 17.9697 14.5304
        curveTo(
          x1 = 18.7374f,
          y1 = 14.8233f,
          x2 = 18.2626f,
          y2 = 14.8233f,
          x3 = 17.9697f,
          y3 = 14.5304f,
        )
        // L 12.1768 8.73753
        lineTo(x = 12.1768f, y = 8.73753f)
        // C 12.0791 8.6399 11.9209 8.6399 11.8232 8.73753
        curveTo(
          x1 = 12.0791f,
          y1 = 8.6399f,
          x2 = 11.9209f,
          y2 = 8.6399f,
          x3 = 11.8232f,
          y3 = 8.73753f,
        )
        // L 6.03033 14.5304
        lineTo(x = 6.03033f, y = 14.5304f)
        // C 5.73744 14.8233 5.26256 14.8233 4.96967 14.5304
        curveTo(
          x1 = 5.73744f,
          y1 = 14.8233f,
          x2 = 5.26256f,
          y2 = 14.8233f,
          x3 = 4.96967f,
          y3 = 14.5304f,
        )
        // C 4.67678 14.2375 4.67678 13.7627 4.96967 13.4698
        curveTo(
          x1 = 4.67678f,
          y1 = 14.2375f,
          x2 = 4.67678f,
          y2 = 13.7627f,
          x3 = 4.96967f,
          y3 = 13.4698f,
        )
        // L 10.7626 7.67687
        lineTo(x = 10.7626f, y = 7.67687f)
        // C 11.446 6.99345 12.554 6.99345 13.2374 7.67687
        curveTo(
          x1 = 11.446f,
          y1 = 6.99345f,
          x2 = 12.554f,
          y2 = 6.99345f,
          x3 = 13.2374f,
          y3 = 7.67687f,
        )
        // L 19.0303 13.4698
        lineTo(x = 19.0303f, y = 13.4698f)
        // C 19.3232 13.7627 19.3232 14.2375 19.0303 14.5304z
        curveTo(
          x1 = 19.3232f,
          y1 = 13.7627f,
          x2 = 19.3232f,
          y2 = 14.2375f,
          x3 = 19.0303f,
          y3 = 14.5304f,
        )
        close()
      }
    }.build().also { _chevronUp = it }
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
        imageVector = HedvigIcons.ChevronUp,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chevronUp: ImageVector? = null
